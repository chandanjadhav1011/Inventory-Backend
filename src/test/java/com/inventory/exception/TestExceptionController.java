package com.inventory.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestExceptionController {

    @GetMapping("/illegal")
    public void illegal() throws IllegalArgumentException {
        throw new IllegalArgumentException("Invalid argument");
    }

    @GetMapping("/duplicate")
    public void duplicate() {
        throw new DuplicateProductException("Duplicate product");
    }

    @GetMapping("/invalid-excel")
    public void invalidExcel() {
        throw new InvalidExcelException("Invalid Excel file");
    }

    @GetMapping("/generic")
    public void generic() {
        throw new RuntimeException("Boom");
    }
}
