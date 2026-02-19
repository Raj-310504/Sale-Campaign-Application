package com.example.SaleCampaign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String productName;
    private Double currentPrice;
    private Double discount;
    private Integer inventory;
}
