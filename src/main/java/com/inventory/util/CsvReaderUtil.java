package com.inventory.util;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.validator.ProductValidator;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class CsvReaderUtil {

    public static ExcelUploadResultDTO readCsv(
            MultipartFile file,
            List<ProductDTO> validProducts
    ) {

        int total = 0;
        int success = 0;
        int failed = 0;

        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                total++;

                try {
                    String[] data = line.split(",");

                    ProductDTO product = ProductDTO.builder()
                            .productSku(data[0])
                            .productName(data[1])
                            .category(data[2])
                            .purchaseDate(java.time.LocalDate.parse(data[3]))
                            .unitPrice(Double.parseDouble(data[4]))
                            .quantity(Integer.parseInt(data[5]))
                            .build();

                    ProductValidator.validate(product);

                    validProducts.add(product);
                    success++;

                } catch (Exception ex) {
                    failed++;
                }
            }

        } catch (Exception e) {
            failed++;
        }

        return ExcelUploadResultDTO.builder()
                .totalRows(total)
                .successCount(success)
                .failedCount(failed)
                .build();
    }
}
