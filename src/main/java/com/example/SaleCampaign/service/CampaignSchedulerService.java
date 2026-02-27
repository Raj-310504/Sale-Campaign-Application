package com.example.SaleCampaign.service;

import com.example.SaleCampaign.entity.CampaignModel;
import com.example.SaleCampaign.entity.CampaignProduct;
import com.example.SaleCampaign.entity.ProductModel;
import com.example.SaleCampaign.repository.CampaignRepository;
import com.example.SaleCampaign.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class CampaignSchedulerService {
    private static final Logger log = LoggerFactory.getLogger(CampaignSchedulerService.class);
    private static final int CHUNK_SIZE = 200;

    private final CampaignRepository campaignRepository;
    private final ProductRepository productRepository;
    private final CampaignDiscountWorkerService campaignDiscountWorkerService;

    public CampaignSchedulerService(
            CampaignRepository campaignRepository,
            ProductRepository productRepository,
            CampaignDiscountWorkerService campaignDiscountWorkerService
    ) {
        this.campaignRepository = campaignRepository;
        this.productRepository = productRepository;
        this.campaignDiscountWorkerService = campaignDiscountWorkerService;
    }


    @Scheduled(cron = "0 * * * * ?")
    public void refreshCampaignsDiscounts() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime changedAt = LocalDateTime.now();

            // Get Active campaign
            List<CampaignModel> active = campaignRepository
                    .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);

            // Sum discount per product
            Map<Long, Double> totalDiscountByProduct = new HashMap<>();
            for (CampaignModel c : active) {
                for (CampaignProduct cp : c.getCampaignProducts()) {
                    Long pid = cp.getProduct().getId();
                    totalDiscountByProduct.merge(pid, safeDouble(cp.getDiscount()), Double::sum);
                }
            }

            // collect products to process
            Set<Long> productIdsToProcess = new HashSet<>(totalDiscountByProduct.keySet());

            for (ProductModel product : productRepository.findByDiscountGreaterThan(0.0)) {
                productIdsToProcess.add(product.getId());
            }
            for (ProductModel product : productRepository.findByBaseCurrentPriceIsNotNullOrBaseDiscountIsNotNull()) {
                productIdsToProcess.add(product.getId());
            }
            if (productIdsToProcess.isEmpty()) {
                return;
            }

            List<Long> allProductIds = new ArrayList<>(productIdsToProcess);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int start = 0; start < allProductIds.size(); start += CHUNK_SIZE) {
                int end = Math.min(start + CHUNK_SIZE, allProductIds.size());
                List<Long> chunk = allProductIds.subList(start, end);
                futures.add(campaignDiscountWorkerService.processProductChunk(chunk, totalDiscountByProduct, changedAt));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (DataAccessException ex) {
            log.error("Failed to refresh campaign discounts due to database error.", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while refreshing campaign discounts.", ex);
        }
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}
