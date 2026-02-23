package com.example.SaleCampaign.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;

    private Double mrp;

    private Double currentPrice;

    private Double discount;

    // Pre-campaign values, used to restore product pricing after campaign ends.
    private Double baseCurrentPrice;

    private Double baseDiscount;

    private Integer inventory;
}
