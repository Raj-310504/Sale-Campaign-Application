package com.example.SaleCampaign.service;

import com.example.SaleCampaign.dto.CreateProductRequestDTO;
import com.example.SaleCampaign.dto.PaginationResponse;
import com.example.SaleCampaign.dto.ProductPriceHistoryResponseDTO;
import com.example.SaleCampaign.dto.ProductResponseDTO;
import com.example.SaleCampaign.entity.ProductModel;
import com.example.SaleCampaign.entity.ProductPriceHistory;
import com.example.SaleCampaign.repository.ProductPriceHistoryRepository;
import com.example.SaleCampaign.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductPriceHistoryRepository productPriceHistoryRepository;

    public ProductResponseDTO addProduct(CreateProductRequestDTO dto) {
        ProductModel product = new ProductModel();
        product.setProductName(dto.getProductName());
        product.setMrp(dto.getMrp());
        product.setDiscount(dto.getDiscount());
        product.setCurrentPrice(dto.getCurrentPrice());
        product.setInventory(dto.getInventory());

        ProductModel savedProduct = productRepository.save(product);
        return new ProductResponseDTO(
                savedProduct.getId(),
                savedProduct.getProductName(),
                savedProduct.getCurrentPrice(),
                savedProduct.getDiscount(),
                savedProduct.getInventory()
        );
    }

    public PaginationResponse<ProductResponseDTO> getAllProducts(Pageable pageable) {

        Page<ProductModel> productPage = productRepository.findAll(pageable);

        Page<ProductResponseDTO> dtoPage = productPage.map(product ->
                new ProductResponseDTO(
                        product.getId(),
                        product.getProductName(),
                        product.getCurrentPrice(),
                        product.getDiscount(),
                        product.getInventory()
                )
        );

        return new PaginationResponse<>(
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.getContent()
        );
    }

    public PaginationResponse<ProductPriceHistoryResponseDTO> getProductPriceHistory(Long productId, Pageable pageable) {
        Page<ProductPriceHistory> historyPage = productPriceHistoryRepository
                .findByProductIdOrderByChangedAtDesc(productId, pageable);

        Page<ProductPriceHistoryResponseDTO> dtoPage = historyPage.map(history ->
                new ProductPriceHistoryResponseDTO(
                        history.getId(),
                        history.getMrp(),
                        history.getDiscount(),
                        history.getCurrentPrice(),
                        history.getChangedAt(),
                        history.getReason()
                )
        );

        return new PaginationResponse<>(
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.getContent()
        );
    }

}
