package com.reservalink.api.application.service.user;

import com.reservalink.api.domain.FeatureName;

import java.util.List;

public interface FeatureLifecycleService {
    void renew(List<String> premiumFeatureIds);

    boolean canUse(String subscriptionId, FeatureName feature);

    void consume(String subscriptionId, FeatureName feature);
}