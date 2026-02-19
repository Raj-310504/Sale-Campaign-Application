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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CampaignSchedularService {
    @Autowired
    private CampaignRepository campaignRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductPriceHistoryRepository productPriceHistoryRepository;


    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void refreshCampaignsDiscounts() {
        LocalDateTime now = LocalDateTime.now();

        // Active campaign
        List<CampaignModel> active = campaignRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now);

        // If 2 campaigns at same time, get max discount
        Map<Long, Double> maxDiscountByProduct = new HashMap<>();
        for (CampaignModel c : active) {
            for (CampaignProduct cp : c.getCampaignProducts()) {
                Long pid = cp.getProduct().getId();
                maxDiscountByProduct.merge(pid, safeDouble(cp.getDiscount()), Math::max);
            }
        }

        Set<Long> productIdsToProcess = new HashSet<>(maxDiscountByProduct.keySet());
        for (ProductModel product : productRepository.findByDiscountGreaterThan(0.0)) {
            productIdsToProcess.add(product.getId());
        }
        if (productIdsToProcess.isEmpty()) {
            return;
        }

        List<ProductModel> productsToProcess = productRepository.findAllById(productIdsToProcess);
        for (ProductModel product : productsToProcess) {
            double currentDiscount = safeDouble(product.getDiscount());
            double targetDiscount = maxDiscountByProduct.getOrDefault(product.getId(), 0.0);
            double targetPrice = safeDouble(product.getMrp()) * (1 - targetDiscount / 100.0);

            if (hasChanged(product.getDiscount(), targetDiscount)
                    || hasChanged(product.getCurrentPrice(), targetPrice)) {
                ProductPriceHistory history = new ProductPriceHistory();
                history.setProduct(product);
                history.setMrp(product.getMrp());
                history.setDiscount(targetDiscount);
                history.setCurrentPrice(targetPrice);
                history.setChangedAt(now);
                history.setReason(resolveReason(currentDiscount, targetDiscount));

                productPriceHistoryRepository.save(history);

                product.setDiscount(targetDiscount);
                product.setCurrentPrice(targetPrice);
            }
        }
        productRepository.saveAll(productsToProcess);
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
