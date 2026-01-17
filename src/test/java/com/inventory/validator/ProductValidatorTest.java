package com.inventory.validator;

import com.inventory.dto.ProductDTO;
import com.inventory.exception.InvalidExcelException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProductValidatorTest {

    private ProductDTO validProduct() {
        return ProductDTO.builder()
                .productSku("SKU-001")
                .productName("Laptop")
                .category("Electronics")
                .purchaseDate(LocalDate.now().minusDays(5))
                .unitPrice(50000)
                .quantity(2)
                .build();
    }


    @Test
    void validate_shouldPassForValidProduct() {
        ProductDTO product = validProduct();

        assertDoesNotThrow(() -> ProductValidator.validate(product));
    }


    @Test
    void validate_shouldFail_whenSkuIsMissing() {
        ProductDTO product = validProduct();
        product.setProductSku("");

        InvalidExcelException ex = assertThrows(
                InvalidExcelException.class,
                () -> ProductValidator.validate(product)
        );

        assertEquals("Product SKU is mandatory", ex.getMessage());
    }

    @Test
    void validate_shouldFail_whenProductNameIsMissing() {
        ProductDTO product = validProduct();
        product.setProductName(null);

        InvalidExcelException ex = assertThrows(
                InvalidExcelException.class,
                () -> ProductValidator.validate(product)
        );

        assertEquals("Product Name is mandatory", ex.getMessage());
    }

    @Test
    void validate_shouldFail_whenCategoryIsMissing() {
        ProductDTO product = validProduct();
        product.setCategory(" ");

        InvalidExcelException ex = assertThrows(
                InvalidExcelException.class,
                () -> ProductValidator.validate(product)
        );

        assertEquals("Category is mandatory", ex.getMessage());
    }

    @Test
    void validate_shouldFail_whenPurchaseDateIsNull() {
        ProductDTO product = validProduct();
        product.setPurchaseDate(null);

        InvalidExcelException ex = assertThrows(
                InvalidExcelException.class,
                () -> ProductValidator.validate(product)
        );

        assertEquals("Purchase Date is mandatory", ex.getMessage());
    }

    @Test
    void validate_shouldFail_whenPurchaseDateIsInFuture() {
        ProductDTO product = validProduct();
        product.setPurchaseDate(LocalDate.now().plusDays(1));

        InvalidExcelException ex = assertThrows(
                InvalidExcelException.class,
                () -> ProductValidator.validate(product)
        );

        assertEquals("Purchase Date cannot be in future", ex.getMessage());
    }

    @Test
    void validate_shouldFail_whenUnitPriceIsZeroOrNegative() {
        ProductDTO product = validProduct();
        product.setUnitPrice(0);

        InvalidExcelException ex = assertThrows(
                InvalidExcelException.class,
                () -> ProductValidator.validate(product)
        );

        assertEquals("Unit Price must be greater than zero", ex.getMessage());
    }

    @Test
    void validate_shouldFail_whenQuantityIsZeroOrNegative() {
        ProductDTO product = validProduct();
        product.setQuantity(-1);

        InvalidExcelException ex = assertThrows(
                InvalidExcelException.class,
                () -> ProductValidator.validate(product)
        );

        assertEquals("Quantity must be greater than zero", ex.getMessage());
    }
}
