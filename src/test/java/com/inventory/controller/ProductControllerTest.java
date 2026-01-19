package com.inventory.controller;

import com.inventory.dto.*;
import com.inventory.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    private MockMvc mockMvc;
    private ProductServiceImpl productService;

    @BeforeEach
    void setup() {
        productService = Mockito.mock(ProductServiceImpl.class);
        ProductController controller = new ProductController(productService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void uploadExcel_shouldReturnSuccess() throws Exception {

        ExcelUploadResultDTO result = ExcelUploadResultDTO.builder()
                .totalRows(10)
                .successCount(10)
                .failedCount(0)
                .build();

        Mockito.when(productService.uploadFile(any()))
                .thenReturn(result);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "products.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy-content".getBytes()
        );

        mockMvc.perform(multipart("/api/products/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Excel processed successfully"))
                .andExpect(jsonPath("$.data.totalRows").value(10))
                .andExpect(jsonPath("$.data.successCount").value(10))
                .andExpect(jsonPath("$.data.failedCount").value(0));
    }

    @Test
    void getProducts_shouldReturnPaginatedResult() throws Exception {

        ProductDTO product = ProductDTO.builder()
                .productSku("SKU001")
                .productName("Laptop")
                .category("Electronics")
                .purchaseDate(LocalDate.now())
                .unitPrice(50000)
                .quantity(2)
                .build();

        PageResponseDTO<ProductDTO> pageResponse =
                PageResponseDTO.<ProductDTO>builder()
                        .content(List.of(product))
                        .page(0)
                        .size(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build();

        Mockito.when(productService.getProducts(
                        anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "productName")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].productSku").value("SKU001"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getSummary_shouldReturnSummary() throws Exception {

        ProductSummaryDTO summary = ProductSummaryDTO.builder()
                .totalProducts(5)
                .totalInventoryValue(250000)
                .averageStockAge(30)
                .build();

        Mockito.when(productService.getSummary())
                .thenReturn(summary);

        mockMvc.perform(get("/api/products/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalProducts").value(5))
                .andExpect(jsonPath("$.data.totalInventoryValue").value(250000));
    }


    @Test
    void clearInventoryDb_shouldReturnSuccess() throws Exception {

        Mockito.doNothing().when(productService).clearInventoryDb();

        mockMvc.perform(delete("/api/products/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Inventory database cleared successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }


    @Test
    void clearInventoryDb_shouldInvokeService() throws Exception {

        Mockito.doNothing().when(productService).clearInventoryDb();

        mockMvc.perform(delete("/api/products/clear"))
                .andExpect(status().isOk());

        Mockito.verify(productService, Mockito.times(1)).clearInventoryDb();
    }
}
