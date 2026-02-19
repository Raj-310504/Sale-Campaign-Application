package com.example.SaleCampaign.repository;

import com.example.SaleCampaign.entity.ProductPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, Long> {
    Page<ProductPriceHistory> findByProductIdOrderByChangedAtDesc(Long productId, Pageable pageable);
}
