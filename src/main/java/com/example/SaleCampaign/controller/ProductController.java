package com.example.SaleCampaign.controller;

import com.example.SaleCampaign.dto.CreateProductRequestDTO;
import com.example.SaleCampaign.dto.PaginationResponse;
import com.example.SaleCampaign.dto.ProductPriceHistoryResponseDTO;
import com.example.SaleCampaign.dto.ProductResponseDTO;
import com.example.SaleCampaign.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/add-product")
    public ResponseEntity<ProductResponseDTO> addProduct(@Valid @RequestBody CreateProductRequestDTO createProductRequestDTO) {
        return ResponseEntity.ok(productService.addProduct(createProductRequestDTO));
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<ProductResponseDTO>> getAllProducts(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/{productId}/price-history")
    public ResponseEntity<PaginationResponse<ProductPriceHistoryResponseDTO>> getProductPriceHistory(
            @PathVariable @Positive(message = "productId must be > 0") Long productId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getProductPriceHistory(productId, pageable));
    }
}
