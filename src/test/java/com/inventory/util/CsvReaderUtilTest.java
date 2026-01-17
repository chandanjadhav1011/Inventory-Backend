package com.inventory.util;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvReaderUtilTest {

    @Test
    void readCsv_shouldProcessAllValidRows() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "products.csv",
                "text/csv",
                getClass().getClassLoader()
                        .getResourceAsStream("product_inventory_10_records.csv")
        );

        List<ProductDTO> validProducts = new ArrayList<>();

        ExcelUploadResultDTO result =
                CsvReaderUtil.readCsv(file, validProducts);

        assertNotNull(result);
        assertEquals(10, result.getTotalRows());
        assertEquals(10, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
        assertEquals(10, validProducts.size());
    }
}
