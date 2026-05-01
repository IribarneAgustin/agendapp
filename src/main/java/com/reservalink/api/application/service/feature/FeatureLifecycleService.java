package com.reservalink.api.application.service.feature;

import com.reservalink.api.domain.enums.FeatureName;

import java.util.List;

public interface FeatureLifecycleService {
    void renew(List<String> premiumFeatureIds, String subscriptionId);

    boolean canUse(String subscriptionId, FeatureName feature);

    void consume(String subscriptionId, FeatureName feature);
}