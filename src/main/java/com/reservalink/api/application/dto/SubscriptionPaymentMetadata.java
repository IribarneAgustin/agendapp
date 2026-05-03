package com.reservalink.api.application.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@SuperBuilder
public class SubscriptionPaymentMetadata extends PaymentMetadata {
    private List<String> premiumFeatureIds;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        if (premiumFeatureIds == null) {
            premiumFeatureIds = Collections.emptyList();
        }
        map.put("premiumFeatures", premiumFeatureIds);
        return map;
    }
}