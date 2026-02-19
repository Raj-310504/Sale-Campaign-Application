package com.example.SaleCampaign.dto;

import com.example.SaleCampaign.enums.PriceChangeReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPriceHistoryResponseDTO {
    private Long id;
    private Double mrp;
    private Double discount;
    private Double currentPrice;
    private LocalDateTime changedAt;
    private PriceChangeReason reason;
}
