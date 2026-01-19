package com.inventory.service.impl;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.dto.ProductSummaryDTO;
import com.inventory.entity.Inventory;
import com.inventory.exception.DuplicateProductException;
import com.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;



    @Test
    void uploadCsv_shouldProcessAllRowsSuccessfully() throws Exception {

        when(inventoryRepository.existsByProductSkuAndPurchaseDate(any(), any()))
                .thenReturn(false);

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "products.csv",
                "text/csv",
                Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("product_inventory_10_records.csv")
                )
        );

        ExcelUploadResultDTO result = productService.uploadFile(csvFile);

        assertNotNull(result);
        assertEquals(10, result.getTotalRows());
        assertEquals(10, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void uploadExcel_shouldSkipInvalidRowsAndReturnCounts() throws Exception {

        when(inventoryRepository.existsByProductSkuAndPurchaseDate(any(), any()))
                .thenReturn(false);

        MockMultipartFile excelFile = new MockMultipartFile(
                "file",
                "products.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("product_inventory_20_with_errors.xlsx")
                )
        );

        ExcelUploadResultDTO result = productService.uploadFile(excelFile);

        assertEquals(20, result.getTotalRows());
        assertEquals(15, result.getSuccessCount());
        assertEquals(5, result.getFailedCount());
        assertFalse(result.getErrors().isEmpty());
    }



    @Test
    void pagination_shouldThrowException_forInvalidPage()
            throws com.inventory.exception.IllegalArgumentException {

        when(inventoryRepository.findAll())
                .thenReturn(mockInventories(10));

        com.inventory.exception.IllegalArgumentException ex = assertThrows(
                com.inventory.exception.IllegalArgumentException.class,
                () -> productService.getProducts(5, 5, "productName", "asc")
        );

        assertEquals("Page number exceeds available data", ex.getMessage());
    }


    @Test
    void getProducts_shouldReturnPagedData()
            throws com.inventory.exception.IllegalArgumentException {

        when(inventoryRepository.findAll())
                .thenReturn(mockInventories(10));

        PageResponseDTO<ProductDTO> page =
                productService.getProducts(0, 5, "productName", "asc");

        assertEquals(5, page.getContent().size());
        assertEquals(10, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertFalse(page.isLast());
    }



    @Test
    void summary_shouldReturnCorrectValues() {

        when(inventoryRepository.findAll())
                .thenReturn(mockInventories(10));

        ProductSummaryDTO summary = productService.getSummary();

        assertEquals(10, summary.getTotalProducts());
        assertTrue(summary.getTotalInventoryValue() > 0);
        assertTrue(summary.getAverageStockAge() >= 0);
    }



    @Test
    void uploadExcel_shouldThrowExceptionForDuplicateSkuAndDate() throws Exception {

        when(inventoryRepository.existsByProductSkuAndPurchaseDate(any(), any()))
                .thenReturn(true);

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "products.csv",
                "text/csv",
                Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("product_inventory_10_records.csv")
                )
        );

        assertThrows(
                DuplicateProductException.class,
                () -> productService.uploadFile(csvFile)
        );
    }



    @Test
    void clearInventoryDb_shouldTruncateTableAndResetIdentity() {

        productService.clearInventoryDb();

        verify(inventoryRepository, times(1)).truncateInventory();
        verify(inventoryRepository, times(1)).resetInventoryIdentity();
    }



    private List<Inventory> mockInventories(int count) {
        List<Inventory> list = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            list.add(
                    Inventory.builder()
                            .id((long) i)
                            .productSku("SKU-" + i)
                            .productName("Product-" + i)
                            .category("Test")
                            .purchaseDate(java.time.LocalDate.now().minusDays(i))
                            .unitPrice(1000)
                            .quantity(1)
                            .build()
            );
        }
        return list;
    }
}

