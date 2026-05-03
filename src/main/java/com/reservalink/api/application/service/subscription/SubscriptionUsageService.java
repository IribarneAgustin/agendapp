package com.reservalink.api.application.service.subscription;

import com.reservalink.api.domain.enums.FeatureName;

public interface SubscriptionUsageService {
    void renew(String userId, String subscriptionId);

    boolean canConsume(String subscriptionId, String planId, FeatureName feature);

    void consume(String subscriptionId, String planId, FeatureName feature);
}