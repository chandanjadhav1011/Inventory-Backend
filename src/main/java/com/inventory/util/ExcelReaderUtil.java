package com.inventory.util;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.exception.InvalidExcelException;
import com.inventory.validator.ProductValidator;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public class ExcelReaderUtil {

    public static ExcelUploadResultDTO readExcel(
            MultipartFile file,
            List<ProductDTO> validProducts
    ) {

        int total = 0;
        int success = 0;
        int failed = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                total++;

                try {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        failed++;
                        continue;
                    }

                    ProductDTO product = ProductDTO.builder()
                            .productSku(row.getCell(0).getStringCellValue())
                            .productName(row.getCell(1).getStringCellValue())
                            .category(row.getCell(2).getStringCellValue())
                            .purchaseDate(row.getCell(3)
                                    .getLocalDateTimeCellValue()
                                    .toLocalDate())
                            .unitPrice(row.getCell(4).getNumericCellValue())
                            .quantity((int) row.getCell(5).getNumericCellValue())
                            .build();

                    ProductValidator.validate(product);

                    validProducts.add(product);
                    success++;

                } catch (Exception ex) {
                    failed++;
                }
            }

        } catch (Exception e) {
            throw new InvalidExcelException("Invalid Excel format");
        }

        return ExcelUploadResultDTO.builder()
                .totalRows(total)
                .successCount(success)
                .failedCount(failed)
                .build();
    }
}
