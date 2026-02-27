package com.example.SaleCampaign.repository;

import com.example.SaleCampaign.entity.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductModel,Long> {
    // campaign end or not or any discount running on product
    List<ProductModel> findByDiscountGreaterThan(Double discount);

    List<ProductModel> findByBaseCurrentPriceIsNotNullOrBaseDiscountIsNotNull();
}
