package com.inventory.service.impl;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.dto.ProductSummaryDTO;
import com.inventory.exception.DuplicateProductException;
import com.inventory.exception.IllegalArgumentException;
import com.inventory.exception.InvalidExcelException;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.IProductService;
import com.inventory.util.CsvReaderUtil;
import com.inventory.util.ExcelReaderUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ProductServiceImpl implements IProductService {

    private final List<ProductDTO> productStore = new ArrayList<>();
    private final Set<String> uniqueKeys = new HashSet<>();

    @PostConstruct
    public void clearOnStartup() {
        productStore.clear();
        uniqueKeys.clear();
        System.out.println("Product store cleared on application startup");
    }


    @Override
    public ExcelUploadResultDTO uploadExcel(MultipartFile file) {

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
            String key = dto.getProductSku() + "_" + dto.getPurchaseDate();

            if (!uniqueKeys.add(key)) {
                throw new DuplicateProductException(
                        "Duplicate Product SKU + Purchase Date found: " + key
                );
            }

            productStore.add(ProductMapper.enrich(dto));
        }

        return result;
    }


    @Override
    public PageResponseDTO<ProductDTO> getProducts(
            int page,
            int size,
            String sortBy,
            String direction
    ) throws IllegalArgumentException {

        if (productStore.isEmpty()) {
            return PageResponseDTO.<ProductDTO>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();

        }

        Comparator<ProductDTO> comparator = getComparator(sortBy);

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        List<ProductDTO> sortedList = productStore.stream()
                .sorted(comparator)
                .toList();

        int totalElements = sortedList.size();
        int start = page * size;

        if (start >= totalElements) {
            throw new IllegalArgumentException("Page number exceeds available data");
        }

        int end = Math.min(start + size, totalElements);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponseDTO.<ProductDTO>builder()
                .content(sortedList.subList(start, end))
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(page == totalPages - 1)
                .build();
    }

    private Comparator<ProductDTO> getComparator(String sortBy) throws IllegalArgumentException {

        if (sortBy == null) {
            return Comparator.comparing(ProductDTO::getProductName);
        }

        return switch (sortBy) {
            case "productName" -> Comparator.comparing(ProductDTO::getProductName);
            case "purchaseDate" -> Comparator.comparing(ProductDTO::getPurchaseDate);
            case "unitPrice" -> Comparator.comparing(ProductDTO::getUnitPrice);
            case "quantity" -> Comparator.comparing(ProductDTO::getQuantity);
            case "stockAge" -> Comparator.comparing(ProductDTO::getStockAge);
            case "inventoryValue" -> Comparator.comparing(ProductDTO::getInventoryValue);
            default -> throw new IllegalArgumentException("Invalid sort field: " + sortBy);

        };
    }



    @Override
    public ProductSummaryDTO getSummary() {
        double totalValue = productStore.stream()
                .mapToDouble(ProductDTO::getInventoryValue)
                .sum();

        double avgAge = productStore.stream()
                .mapToLong(ProductDTO::getStockAge)
                .average()
                .orElse(0);

        return ProductSummaryDTO.builder()
                .totalProducts(productStore.size())
                .totalInventoryValue(totalValue)
                .averageStockAge(avgAge)
                .build();

    }
}


