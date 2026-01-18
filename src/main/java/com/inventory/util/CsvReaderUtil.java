package com.inventory.util;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.validator.ProductValidator;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CsvReaderUtil {

    public static ExcelUploadResultDTO readCsv(
            MultipartFile file,
            List<ProductDTO> validProducts
    ) {

        int total = 0, success = 0, failed = 0;
        List<String> errors = new ArrayList<>();

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
                            .purchaseDate(LocalDate.parse(data[3]))
                            .unitPrice(Double.parseDouble(data[4]))
                            .quantity(Integer.parseInt(data[5]))
                            .build();

                    ProductValidator.validate(product);
                    validProducts.add(product);
                    success++;

                } catch (Exception ex) {
                    failed++;
                    errors.add("Row " + (total + 1) + ": Invalid CSV data");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file");
        }

        return ExcelUploadResultDTO.builder()
                .totalRows(total)
                .successCount(success)
                .failedCount(failed)
                .errors(errors)
                .build();
    }
}
