package com.inventory.util;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.exception.InvalidExcelException;
import com.inventory.validator.ProductValidator;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ExcelReaderUtil {

    public static ExcelUploadResultDTO readExcel(
            MultipartFile file,
            List<ProductDTO> validProducts
    ) {

        int total = 0, success = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            FormulaEvaluator evaluator =
                    workbook.getCreationHelper().createFormulaEvaluator();

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                total++;

                try {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        failed++;
                        errors.add("Row " + (i + 1) + ": Empty row");
                        continue;
                    }

                    ProductDTO product = ProductDTO.builder()
                            .productSku(getCellValue(row.getCell(0), evaluator))
                            .productName(getCellValue(row.getCell(1), evaluator))
                            .category(getCellValue(row.getCell(2), evaluator))
                            .purchaseDate(getDateValue(row.getCell(3), evaluator))
                            .unitPrice(Double.parseDouble(getCellValue(row.getCell(4), evaluator)))
                            .quantity((int) Double.parseDouble(getCellValue(row.getCell(5), evaluator)))
                            .build();

                    ProductValidator.validate(product);
                    validProducts.add(product);
                    success++;

                } catch (Exception e) {
                    failed++;
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new InvalidExcelException("Invalid Excel format");
        }

        return ExcelUploadResultDTO.builder()
                .totalRows(total)
                .successCount(success)
                .failedCount(failed)
                .errors(errors)
                .build();
    }

    private static String getCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case FORMULA -> evaluator.evaluate(cell).formatAsString();
            default -> "";
        };
    }

    private static LocalDate getDateValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        if (cell.getCellType() == CellType.FORMULA) {
            CellValue value = evaluator.evaluate(cell);
            return DateUtil.getJavaDate(value.getNumberValue())
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        return LocalDate.parse(cell.getStringCellValue());
    }
}
