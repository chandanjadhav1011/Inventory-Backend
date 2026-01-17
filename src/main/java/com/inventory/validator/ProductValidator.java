package com.inventory.validator;

import com.inventory.dto.ProductDTO;
import com.inventory.exception.InvalidExcelException;

import java.time.LocalDate;

public class ProductValidator {

    public static void validate(ProductDTO item) {

        if (item.getProductSku() == null || item.getProductSku().isBlank()) {
            throw new InvalidExcelException("Product SKU is mandatory");
        }

        if (item.getProductName() == null || item.getProductName().isBlank()) {
            throw new InvalidExcelException("Product Name is mandatory");
        }

        if (item.getCategory() == null || item.getCategory().isBlank()) {
            throw new InvalidExcelException("Category is mandatory");
        }

        if (item.getPurchaseDate() == null) {
            throw new InvalidExcelException("Purchase Date is mandatory");
        }

        if (item.getPurchaseDate().isAfter(LocalDate.now())) {
            throw new InvalidExcelException("Purchase Date cannot be in future");
        }

        if (item.getUnitPrice() <= 0) {
            throw new InvalidExcelException("Unit Price must be greater than zero");
        }

        if (item.getQuantity() <= 0) {
            throw new InvalidExcelException("Quantity must be greater than zero");
        }
    }
}
