package com.example.SaleCampaign.service;

import com.example.SaleCampaign.entity.ProductModel;
import com.example.SaleCampaign.entity.ProductPriceHistory;
import com.example.SaleCampaign.enums.PriceChangeReason;
import com.example.SaleCampaign.repository.ProductPriceHistoryRepository;
import com.example.SaleCampaign.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class CampaignDiscountWorkerService {

    private final ProductRepository productRepository;
    private final ProductPriceHistoryRepository productPriceHistoryRepository;

    public CampaignDiscountWorkerService(
            ProductRepository productRepository,
            ProductPriceHistoryRepository productPriceHistoryRepository
    ) {
        this.productRepository = productRepository;
        this.productPriceHistoryRepository = productPriceHistoryRepository;
    }

    @Async("campaignExecutor")
    @Transactional
    public CompletableFuture<Void> processProductChunk(
            List<Long> productIds,
            Map<Long, Double> totalDiscountByProduct,
            LocalDateTime changedAt
    ) {
        List<ProductModel> productsToProcess = productRepository.findAllById(productIds);

        for (ProductModel product : productsToProcess) {
            boolean campaignActive = totalDiscountByProduct.containsKey(product.getId());
            double currentDiscount = safeDouble(product.getDiscount());

            if (campaignActive) {
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
            } else if (product.getBaseCurrentPrice() != null || product.getBaseDiscount() != null) {
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
        return CompletableFuture.completedFuture(null);
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
