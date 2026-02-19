package com.example.SaleCampaign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequestDTO {

    private String productName;
    private Double mrp;
    private Double currentPrice;
    private Double discount;
    private Integer inventory;
}
