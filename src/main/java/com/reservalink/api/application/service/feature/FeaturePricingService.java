package com.reservalink.api.application.service.feature;

import com.reservalink.api.domain.FeatureUsage;

import java.math.BigDecimal;
import java.util.List;

public interface FeaturePricingService {
    BigDecimal calculateFeaturesPricing(List<FeatureUsage> features);
}