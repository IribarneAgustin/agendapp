package com.reservalink.api.application.service.feature;

import com.reservalink.api.application.output.SubscriptionFeatureRepositoryPort;
import com.reservalink.api.application.output.SubscriptionRepositoryPort;
import com.reservalink.api.domain.FeatureUsage;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.SubscriptionFeature;
import com.reservalink.api.utils.GenericAppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeaturePricingServiceImpl implements FeaturePricingService {

    private final SubscriptionFeatureRepositoryPort subscriptionFeatureRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;

    @Override
    public BigDecimal calculateFeaturesPricing(List<FeatureUsage> features) {
        if (features == null || features.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<String> featureIds = features.stream()
                .map(FeatureUsage::getSubscriptionFeatureId)
                .distinct()
                .toList();

        Map<String, SubscriptionFeature> subscriptionFeatureMap = subscriptionFeatureRepositoryPort
                .findAllByIds(featureIds)
                .stream()
                .collect(Collectors.toMap(SubscriptionFeature::getId, f -> f));

        return features.stream()
                .map(usage -> calculatePriceForUsage(usage, subscriptionFeatureMap.get(usage.getSubscriptionFeatureId())))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePriceForUsage(FeatureUsage usage, SubscriptionFeature subscriptionFeature) {
        if (subscriptionFeature == null || subscriptionFeature.getPrice() == null) {
            return BigDecimal.ZERO;
        }

        if (usage.isFirstCycle()) {
            return calculateProratedPrice(subscriptionFeature.getPrice(), usage);
        }

        return subscriptionFeature.getPrice();
    }

    private BigDecimal calculateProratedPrice(BigDecimal fullPrice, FeatureUsage usage) {
        Subscription sub = subscriptionRepositoryPort
                .findById(usage.getSubscriptionId())
                .orElseThrow();

        LocalDate cycleEnd = sub.getExpiration().toLocalDate();
        LocalDate activationDate = usage.getActivatedAt().toLocalDate();

        long daysUsed = ChronoUnit.DAYS.between(activationDate, cycleEnd) + 1;
        daysUsed = Math.min(GenericAppConstants.DEFAULT_BILLING_PERIOD_DAYS, Math.max(1, daysUsed));

        BigDecimal dailyPrice = fullPrice.divide(BigDecimal.valueOf(GenericAppConstants.DEFAULT_BILLING_PERIOD_DAYS), 10, RoundingMode.HALF_UP);

        long unusedDays = GenericAppConstants.DEFAULT_BILLING_PERIOD_DAYS - daysUsed;
        BigDecimal discount = dailyPrice.multiply(BigDecimal.valueOf(unusedDays));

        return fullPrice.subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}