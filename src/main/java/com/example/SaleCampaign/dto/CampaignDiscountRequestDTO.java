package com.example.SaleCampaign.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignDiscountRequestDTO {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "discount is required")
    @DecimalMin(value = "0.0", message = "discount must be >= 0")
    @DecimalMax(value = "100.0", message = "discount must be <= 100")
    private Double discount;
}
