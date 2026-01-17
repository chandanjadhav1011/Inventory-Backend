package com.inventory.service.impl;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.dto.ProductSummaryDTO;
import com.inventory.exception.IllegalArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceImplTest {

    private ProductServiceImpl productService;

    @BeforeEach
    void setup() {
        productService = new ProductServiceImpl();
    }


    @Test
    void uploadCsv_shouldProcessAllRowsSuccessfully() throws Exception {

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "products.csv",
                "text/csv",
                Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("product_inventory_10_records.csv")
                )
        );

        ExcelUploadResultDTO result = productService.uploadExcel(csvFile);

        assertNotNull(result);
        assertEquals(10, result.getTotalRows());
        assertEquals(10, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
    }


    @Test
    void uploadExcel_shouldSkipInvalidRowsAndReturnCounts() throws Exception {

        MockMultipartFile excelFile = new MockMultipartFile(
                "file",
                "products.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("product_inventory_20_with_errors.xlsx")
                )
        );

        ExcelUploadResultDTO result = productService.uploadExcel(excelFile);

        assertNotNull(result);
        assertEquals(20, result.getTotalRows());
        assertEquals(15, result.getSuccessCount());
        assertEquals(5, result.getFailedCount());
    }


    @Test
    void pagination_shouldThrowCustomException_forInvalidPage() throws Exception {

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "products.csv",
                "text/csv",
                Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("product_inventory_10_records.csv")
                )
        );
        productService.uploadExcel(csvFile);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> productService.getProducts(5, 5, "productName", "asc")
        );

        assertEquals("Page number exceeds available data", ex.getMessage());
    }


    @Test
    void getProducts_shouldReturnPagedData() throws Exception {

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "products.csv",
                "text/csv",
                Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("product_inventory_10_records.csv")
                )
        );
        productService.uploadExcel(csvFile);

        PageResponseDTO<ProductDTO> page =
                productService.getProducts(0, 5, "productName", "asc");

        assertEquals(5, page.getContent().size());
        assertEquals(10, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertFalse(page.isLast());
    }


    @Test
    void summary_shouldReturnCorrectValues() throws Exception {

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "products.csv",
                "text/csv",
                Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("product_inventory_10_records.csv")
                )
        );
        productService.uploadExcel(csvFile);

        ProductSummaryDTO summary = productService.getSummary();

        assertEquals(10, summary.getTotalProducts());
        assertTrue(summary.getTotalInventoryValue() > 0);
        assertTrue(summary.getAverageStockAge() >= 0);
    }
}
