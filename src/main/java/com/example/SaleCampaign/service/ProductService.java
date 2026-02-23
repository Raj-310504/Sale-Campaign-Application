package com.example.SaleCampaign.service;

import com.example.SaleCampaign.dto.CreateProductRequestDTO;
import com.example.SaleCampaign.dto.PaginationResponse;
import com.example.SaleCampaign.dto.ProductPriceHistoryResponseDTO;
import com.example.SaleCampaign.dto.ProductResponseDTO;
import com.example.SaleCampaign.entity.ProductModel;
import com.example.SaleCampaign.entity.ProductPriceHistory;
import com.example.SaleCampaign.exception.ResourceNotFoundException;
import com.example.SaleCampaign.exception.ServiceException;
import com.example.SaleCampaign.repository.ProductPriceHistoryRepository;
import com.example.SaleCampaign.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
        try {
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
        } catch (DataAccessException ex) {
            throw new ServiceException("Failed to add product due to database error.", ex);
        } catch (Exception ex) {
            throw new ServiceException("Unexpected error while adding product.", ex);
        }
    }

    public PaginationResponse<ProductResponseDTO> getAllProducts(Pageable pageable) {
        try {
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
        } catch (DataAccessException ex) {
            throw new ServiceException("Failed to fetch products due to database error.", ex);
        } catch (Exception ex) {
            throw new ServiceException("Unexpected error while fetching products.", ex);
        }
    }

    public PaginationResponse<ProductPriceHistoryResponseDTO> getProductPriceHistory(Long productId, Pageable pageable) {
        try {
            if (!productRepository.existsById(productId)) {
                throw new ResourceNotFoundException("Product not found: " + productId);
            }

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
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new ServiceException("Failed to fetch product price history due to database error.", ex);
        } catch (Exception ex) {
            throw new ServiceException("Unexpected error while fetching product price history.", ex);
        }
    }

}
