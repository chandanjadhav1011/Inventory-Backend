package com.inventory.service.impl;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.dto.ProductSummaryDTO;
import com.inventory.entity.Inventory;
import com.inventory.exception.DuplicateProductException;
import com.inventory.exception.IllegalArgumentException;
import com.inventory.exception.InvalidExcelException;
import com.inventory.mapper.ProductMapper;
import com.inventory.repository.InventoryRepository;
import com.inventory.service.IProductService;
import com.inventory.util.CsvReaderUtil;
import com.inventory.util.ExcelReaderUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final InventoryRepository inventoryRepository;


    @Override
    public ExcelUploadResultDTO uploadFile(MultipartFile file) {

        inventoryRepository.deleteAll();

        List<ProductDTO> validProducts = new ArrayList<>();

        String filename = Optional.ofNullable(file.getOriginalFilename())
                .orElse("")
                .toLowerCase();

        ExcelUploadResultDTO result;

        if (filename.endsWith(".csv")) {
            result = CsvReaderUtil.readCsv(file, validProducts);
        } else if (filename.endsWith(".xlsx")) {
            result = ExcelReaderUtil.readExcel(file, validProducts);
        } else {
            throw new InvalidExcelException("Only CSV or Excel files are supported");
        }

        for (ProductDTO dto : validProducts) {

            if (inventoryRepository.existsByProductSkuAndPurchaseDate(
                    dto.getProductSku(), dto.getPurchaseDate())) {

                throw new DuplicateProductException(
                        "Duplicate Product SKU + Purchase Date: "
                                + dto.getProductSku() + " - " + dto.getPurchaseDate()
                );
            }

            Inventory inventory = Inventory.builder()
                    .productSku(dto.getProductSku())
                    .productName(dto.getProductName())
                    .category(dto.getCategory())
                    .purchaseDate(dto.getPurchaseDate())
                    .unitPrice(dto.getUnitPrice())
                    .quantity(dto.getQuantity())
                    .build();

            inventoryRepository.save(inventory);
        }

        return result;
    }

    @Override
    public PageResponseDTO<ProductDTO> getProducts(
            int page, int size, String sortBy, String direction) throws IllegalArgumentException {

        List<Inventory> inventories = inventoryRepository.findAll();

        if (inventories.isEmpty()) {
            return PageResponseDTO.<ProductDTO>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();
        }

        List<ProductDTO> products = inventories.stream()
                .map(ProductMapper::enrich)
                .toList();

        Comparator<ProductDTO> comparator = getComparator(sortBy);
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        List<ProductDTO> sorted = products.stream()
                .sorted(comparator)
                .toList();

        int totalElements = sorted.size();
        int start = page * size;

        if (start >= totalElements) {
            throw new IllegalArgumentException("Page number exceeds available data");
        }

        int end = Math.min(start + size, totalElements);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponseDTO.<ProductDTO>builder()
                .content(sorted.subList(start, end))
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(page == totalPages - 1)
                .build();
    }

    private Comparator<ProductDTO> getComparator(String sortBy) {
        return switch (sortBy) {
            case "productName" -> Comparator.comparing(ProductDTO::getProductName);
            case "purchaseDate" -> Comparator.comparing(ProductDTO::getPurchaseDate);
            case "unitPrice" -> Comparator.comparing(ProductDTO::getUnitPrice);
            case "quantity" -> Comparator.comparing(ProductDTO::getQuantity);
            case "stockAge" -> Comparator.comparing(ProductDTO::getStockAge);
            case "inventoryValue" -> Comparator.comparing(ProductDTO::getInventoryValue);
            default -> Comparator.comparing(ProductDTO::getProductName);
        };
    }

    @Override
    public ProductSummaryDTO getSummary() {

        List<Inventory> inventories = inventoryRepository.findAll();

        double totalValue = inventories.stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantity())
                .sum();

        double avgAge = inventories.stream()
                .mapToLong(i -> ProductMapper.calculateStockAge(i.getPurchaseDate()))
                .average()
                .orElse(0);

        avgAge = Math.round(avgAge * 100.0) / 100.0;

        return ProductSummaryDTO.builder()
                .totalProducts(inventories.size())
                .totalInventoryValue(totalValue)
                .averageStockAge(avgAge)
                .build();
    }

    @Override
    @Transactional
    public void clearInventoryDb() {
        inventoryRepository.truncateInventory();
        inventoryRepository.resetInventoryIdentity();
    }
}

