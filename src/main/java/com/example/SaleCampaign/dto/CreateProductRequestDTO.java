package com.example.SaleCampaign.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequestDTO {

    @NotBlank(message = "productName is required")
    private String productName;

    @NotNull(message = "mrp is required")
    @DecimalMin(value = "0.0", message = "mrp must be >= 0")
    private Double mrp;

    @NotNull(message = "currentPrice is required")
    @DecimalMin(value = "0.0", message = "currentPrice must be >= 0")
    private Double currentPrice;

    @NotNull(message = "discount is required")
    @DecimalMin(value = "0.0", message = "discount must be >= 0")
    @DecimalMax(value = "100.0", message = "discount must be <= 100")
    private Double discount;

    @NotNull(message = "inventory is required")
    @Min(value = 0, message = "inventory must be >= 0")
    private Integer inventory;

    @AssertTrue(message = "currentPrice must be <= mrp")
    public boolean isCurrentPriceNotAboveMrp() {
        if (mrp == null || currentPrice == null) {
            return true;
        }
        return currentPrice <= mrp;
    }
}
