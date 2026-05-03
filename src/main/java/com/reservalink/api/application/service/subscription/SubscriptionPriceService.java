package com.reservalink.api.application.service.subscription;

import com.reservalink.api.application.output.FeatureUsageRepositoryPort;
import com.reservalink.api.application.service.feature.FeaturePricingService;
import com.reservalink.api.domain.FeatureUsage;
import com.reservalink.api.domain.SubscriptionPlan;
import com.reservalink.api.domain.enums.SubscriptionPlanCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class SubscriptionPriceService {

    private static final BigDecimal PRICE_PER_RESOURCE = new BigDecimal("6000.00");
    private final FeaturePricingService featurePricingService;

    public BigDecimal calculateTotal(SubscriptionPlan plan, Integer selectedResources, List<FeatureUsage> addOnFeatures) {
        BigDecimal featuresPrice = featurePricingService.calculateFeaturesPricing(addOnFeatures);
        BigDecimal total = plan.getPrice();

        if (SubscriptionPlanCode.PROFESSIONAL == plan.getCode() && selectedResources != null) {
            int included = 2;
            int extras = Math.max(0, selectedResources - included);

            if (extras > 0) {
                BigDecimal extraResourcesCost = PRICE_PER_RESOURCE.multiply(BigDecimal.valueOf(extras));
                total = total.add(extraResourcesCost);
            }
        }
        return total.add(featuresPrice);
    }
}