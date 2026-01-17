package com.inventory.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProductDTO {

    private String productSku;
    private String productName;
    private String category;
    private LocalDate purchaseDate;
    private double unitPrice;
    private int quantity;

    private long stockAge;
    private double inventoryValue;
}
