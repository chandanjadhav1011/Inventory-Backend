package com.inventory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSummaryDTO {

    private int totalProducts;
    private double totalInventoryValue;
    private double averageStockAge;
}
