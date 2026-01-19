package com.inventory.service;

import com.inventory.dto.ExcelUploadResultDTO;
import com.inventory.dto.PageResponseDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.dto.ProductSummaryDTO;
import com.inventory.exception.IllegalArgumentException;
import org.springframework.web.multipart.MultipartFile;

public interface IProductService {

    public ExcelUploadResultDTO uploadFile(MultipartFile file);

    public PageResponseDTO<ProductDTO> getProducts(int page, int size, String sortBy, String direction) throws IllegalArgumentException;
    public ProductSummaryDTO getSummary();


}
