package com.inventory.controller;


import com.inventory.dto.*;
import com.inventory.exception.IllegalArgumentException;
import com.inventory.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductServiceImpl productService;

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ApiResponse<ExcelUploadResultDTO>> uploadExcel(
            @RequestPart("file") MultipartFile file
    ) {
        ExcelUploadResultDTO result = productService.uploadFile(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<ExcelUploadResultDTO>builder()
                        .success(true)
                        .message("Excel processed successfully")
                        .data(result)
                        .timestamp(LocalDateTime.now())
                        .build());
    }


    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<ProductDTO>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) throws IllegalArgumentException {
        PageResponseDTO<ProductDTO> response =
                productService.getProducts(page, size, sortBy, direction);

        return ResponseEntity.ok(
                ApiResponse.<PageResponseDTO<ProductDTO>>builder()
                        .success(true)
                        .message("Products fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }


    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ProductSummaryDTO>> summary() {

        return ResponseEntity.ok(
                ApiResponse.<ProductSummaryDTO>builder()
                        .success(true)
                        .message("Product summary fetched successfully")
                        .data(productService.getSummary())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearInventoryDb() {
        productService.clearInventoryDb();
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Inventory database cleared successfully")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}
