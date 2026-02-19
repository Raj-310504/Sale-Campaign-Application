package com.example.SaleCampaign.entity;

import com.example.SaleCampaign.enums.PriceChangeReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductModel product;

    private Double mrp;

    private Double discount;

    private Double currentPrice;

    private LocalDateTime changedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private PriceChangeReason reason;
}
