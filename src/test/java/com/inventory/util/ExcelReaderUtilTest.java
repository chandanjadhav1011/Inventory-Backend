package com.inventory.util;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelReaderUtilTest {

    @Test
    void readExcel_shouldProcessValidAndInvalidRows() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "products.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                getClass().getClassLoader()
                        .getResourceAsStream("product_inventory_20_with_errors.xlsx")
        );

        List<ProductDTO> validProducts = new ArrayList<>();

        ExcelUploadResultDTO result =
                ExcelReaderUtil.readExcel(file, validProducts);

        assertNotNull(result);
        assertEquals(20, result.getTotalRows());
        assertEquals(15, result.getSuccessCount());
        assertEquals(5, result.getFailedCount());
        assertEquals(15, validProducts.size());
    }
}
