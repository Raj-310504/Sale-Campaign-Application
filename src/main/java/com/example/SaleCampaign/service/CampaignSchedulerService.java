package com.example.SaleCampaign.service;

import com.example.SaleCampaign.entity.CampaignModel;
import com.example.SaleCampaign.entity.CampaignProduct;
import com.example.SaleCampaign.enums.PriceChangeReason;
import com.example.SaleCampaign.entity.ProductModel;
import com.example.SaleCampaign.entity.ProductPriceHistory;
import com.example.SaleCampaign.repository.CampaignRepository;
import com.example.SaleCampaign.repository.ProductPriceHistoryRepository;
import com.example.SaleCampaign.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class CampaignSchedulerService {
    private static final Logger log = LoggerFactory.getLogger(CampaignSchedulerService.class);

    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductPriceHistoryRepository productPriceHistoryRepository;


    @Scheduled(cron = "0 * * * * ?")
    @Transactional
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

            List<ProductModel> productsToProcess = productRepository.findAllById(productIdsToProcess);

            for (ProductModel product : productsToProcess) {
                boolean campaignActive = totalDiscountByProduct.containsKey(product.getId());
                double currentDiscount = safeDouble(product.getDiscount());

                if (campaignActive) {
                    // save campaign price once
                    if (product.getBaseCurrentPrice() == null) {
                        product.setBaseCurrentPrice(product.getCurrentPrice());
                    }
                    if (product.getBaseDiscount() == null) {
                        product.setBaseDiscount(product.getDiscount());
                    }

                    double targetDiscount = Math.max(0.0,
                            Math.min(totalDiscountByProduct.getOrDefault(product.getId(), 0.0), 100.0));
                    double targetPrice = safeDouble(product.getMrp()) * (1 - targetDiscount / 100.0);

                    if (hasChanged(product.getDiscount(), targetDiscount)
                            || hasChanged(product.getCurrentPrice(), targetPrice)) {
                        ProductPriceHistory history = new ProductPriceHistory();
                        history.setProduct(product);
                        history.setMrp(product.getMrp());
                        history.setDiscount(targetDiscount);
                        history.setCurrentPrice(targetPrice);
                        history.setChangedAt(changedAt);
                        history.setReason(resolveReason(currentDiscount, targetDiscount));

                        productPriceHistoryRepository.save(history);
                        product.setDiscount(targetDiscount);
                        product.setCurrentPrice(targetPrice);
                    }
                }
                // Campaign end
                else if (product.getBaseCurrentPrice() != null || product.getBaseDiscount() != null) {
                    double restoreDiscount = safeDouble(product.getBaseDiscount());
                    double restorePrice = product.getBaseCurrentPrice() == null
                            ? safeDouble(product.getMrp())
                            : product.getBaseCurrentPrice();

                    if (hasChanged(product.getDiscount(), restoreDiscount)
                            || hasChanged(product.getCurrentPrice(), restorePrice)) {
                        ProductPriceHistory history = new ProductPriceHistory();
                        history.setProduct(product);
                        history.setMrp(product.getMrp());
                        history.setDiscount(restoreDiscount);
                        history.setCurrentPrice(restorePrice);
                        history.setChangedAt(changedAt);
                        history.setReason(PriceChangeReason.CAMPAIGN_ENDED);

                        productPriceHistoryRepository.save(history);
                        product.setDiscount(restoreDiscount);
                        product.setCurrentPrice(restorePrice);
                    }

                    product.setBaseDiscount(null);
                    product.setBaseCurrentPrice(null);
                }
            }
            productRepository.saveAll(productsToProcess);
        } catch (DataAccessException ex) {
            log.error("Failed to refresh campaign discounts due to database error.", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while refreshing campaign discounts.", ex);
        }
    }

    private boolean hasChanged(Double current, double target) {
        return current == null || toScale2(current) != toScale2(target);
    }

    private PriceChangeReason resolveReason(double currentDiscount, double targetDiscount) {
        boolean wasActive = isDiscountActive(currentDiscount);
        boolean isActive = isDiscountActive(targetDiscount);
        if (!wasActive && isActive) {
            return PriceChangeReason.CAMPAIGN_APPLIED;
        }
        if (wasActive && !isActive) {
            return PriceChangeReason.CAMPAIGN_ENDED;
        }
        return PriceChangeReason.CAMPAIGN_UPDATED;
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private boolean isDiscountActive(double discount) {
        return toScale2(discount) > 0;
    }

    private long toScale2(double value) {
        return Math.round(value * 100.0);
    }
}
