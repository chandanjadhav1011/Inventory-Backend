package com.inventory.mapper;

import com.inventory.dto.ProductDTO;
import com.inventory.entity.Inventory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProductMapper {

    public static ProductDTO enrich(Inventory inventory) {

        long age = calculateStockAge(inventory.getPurchaseDate());
        double inventoryValue = inventory.getUnitPrice() * inventory.getQuantity();

        return ProductDTO.builder()
                .productSku(inventory.getProductSku())
                .productName(inventory.getProductName())
                .category(inventory.getCategory())
                .purchaseDate(inventory.getPurchaseDate())
                .unitPrice(inventory.getUnitPrice())
                .quantity(inventory.getQuantity())
                .stockAge(age)
                .inventoryValue(inventoryValue)
                .build();
    }

    public static long calculateStockAge(LocalDate purchaseDate) {
        return ChronoUnit.DAYS.between(purchaseDate, LocalDate.now());
    }
}
