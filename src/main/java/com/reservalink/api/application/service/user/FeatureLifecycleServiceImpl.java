package com.reservalink.api.application.service.user;

import com.reservalink.api.application.output.FeatureUsageRepositoryPort;
import com.reservalink.api.domain.FeatureName;
import com.reservalink.api.domain.FeatureStatus;
import com.reservalink.api.domain.FeatureUsage;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FeatureLifecycleServiceImpl implements FeatureLifecycleService {

    private final FeatureUsageRepositoryPort featureUsageRepositoryPort;

    public FeatureLifecycleServiceImpl(FeatureUsageRepositoryPort featureUsageRepositoryPort) {
        this.featureUsageRepositoryPort = featureUsageRepositoryPort;
    }

    @Override
    public void renew(List<String> premiumFeatureIds) {
        List<FeatureUsage> expiredFeatures = featureUsageRepositoryPort.findAllById(premiumFeatureIds);
        expiredFeatures.forEach(expiredFeatureUsage -> {

            expiredFeatureUsage.setFeatureStatus(FeatureStatus.EXPIRED);
            featureUsageRepositoryPort.update(expiredFeatureUsage);

            FeatureUsage newFeatureUsage = FeatureUsage.builder()
                    .featureStatus(FeatureStatus.ACTIVE)
                    .subscriptionId(expiredFeatureUsage.getSubscriptionId())
                    .subscriptionFeatureId(expiredFeatureUsage.getSubscriptionFeatureId())
                    .enabled(Boolean.TRUE)
                    .usage(0)
                    .build();

            featureUsageRepositoryPort.create(newFeatureUsage);
        });
    }

    @Override
    public boolean canUse(String subscriptionId, FeatureName featureName) {
        return featureUsageRepositoryPort
                .findLatestActiveAvailableBySubscriptionIdAndFeatureName(subscriptionId, featureName)
                .isPresent();
    }

    @Override
    public void consume(String subscriptionId, FeatureName featureName) {
        FeatureUsage featureUsage = featureUsageRepositoryPort
                .findLatestActiveAvailableBySubscriptionIdAndFeatureName(subscriptionId, featureName)
                .orElseThrow(() -> new BusinessRuleException(BusinessErrorCodes.FEATURE_LIMIT_EXCEEDED.name()));
        featureUsage.setUsage(featureUsage.getUsage() + 1);
        featureUsageRepositoryPort.update(featureUsage);
        log.info("Feature {} consumed for subscription {}.", featureName, subscriptionId);
    }
}
