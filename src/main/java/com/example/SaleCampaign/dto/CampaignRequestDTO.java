package com.example.SaleCampaign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignRequestDTO {

    @NotBlank(message = "title is required")
    private String title;

    @NotNull(message = "startDate is required")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate endDate;

    @NotEmpty(message = "campaignDiscount is required")
    @Valid
    private List<CampaignDiscountRequestDTO> campaignDiscount;

    @AssertTrue(message = "endDate must be on or after startDate")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "campaignDiscount contains duplicate productId values")
    public boolean isCampaignDiscountUniqueByProduct() {
        if (campaignDiscount == null || campaignDiscount.isEmpty()) {
            return true;
        }
        Set<Long> productIds = new HashSet<>();
        for (CampaignDiscountRequestDTO item : campaignDiscount) {
            if (item == null || item.getProductId() == null) {
                continue;
            }
            if (!productIds.add(item.getProductId())) {
                return false;
            }
        }
        return true;
    }
}
