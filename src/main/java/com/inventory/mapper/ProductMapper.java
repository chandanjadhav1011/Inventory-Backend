package com.inventory.mapper;

import com.inventory.dto.ProductDTO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProductMapper {

    public static ProductDTO enrich(ProductDTO dto) {

        long age = ChronoUnit.DAYS.between(dto.getPurchaseDate(), LocalDate.now());
        double inventoryValue = dto.getUnitPrice() * dto.getQuantity();

        return ProductDTO.builder()
                .productSku(dto.getProductSku())
                .productName(dto.getProductName())
                .category(dto.getCategory())
                .purchaseDate(dto.getPurchaseDate())
                .unitPrice(dto.getUnitPrice())
                .quantity(dto.getQuantity())
                .stockAge(age)
                .inventoryValue(inventoryValue)
                .build();
    }
}
