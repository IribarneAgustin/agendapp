package com.reservalink.api.application.service.feature;

import com.reservalink.api.application.output.FeatureUsageRepositoryPort;
import com.reservalink.api.application.output.SubscriptionFeatureRepositoryPort;
import com.reservalink.api.domain.enums.FeatureName;
import com.reservalink.api.domain.enums.FeatureStatus;
import com.reservalink.api.domain.FeatureUsage;
import com.reservalink.api.domain.SubscriptionFeature;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FeatureLifecycleServiceImpl implements FeatureLifecycleService {

    private final FeatureUsageRepositoryPort featureUsageRepositoryPort;

    private final SubscriptionFeatureRepositoryPort subscriptionFeatureRepositoryPort;

    public FeatureLifecycleServiceImpl(FeatureUsageRepositoryPort featureUsageRepositoryPort,
                                       SubscriptionFeatureRepositoryPort subscriptionFeatureRepositoryPort) {
        this.featureUsageRepositoryPort = featureUsageRepositoryPort;
        this.subscriptionFeatureRepositoryPort = subscriptionFeatureRepositoryPort;
    }

    @Override
    public void renew(List<String> premiumFeatureIds, String subscriptionId) {
        List<SubscriptionFeature> expiredFeatures = subscriptionFeatureRepositoryPort.findAllByIds(premiumFeatureIds);
        expiredFeatures.forEach(expiredFeatureUsage -> {
            featureUsageRepositoryPort.findByUserSubscriptionIdAndFeatureNameAndStatus(subscriptionId, expiredFeatureUsage.getName() , FeatureStatus.ACTIVE)
                    .ifPresent(expiredUsage -> {
                        expiredUsage.setFeatureStatus(FeatureStatus.EXPIRED);
                        featureUsageRepositoryPort.update(expiredUsage);
                    });

            FeatureUsage newFeatureUsage = FeatureUsage.builder()
                    .featureStatus(FeatureStatus.ACTIVE)
                    .subscriptionId(subscriptionId)
                    .subscriptionFeatureId(expiredFeatureUsage.getId())
                    .enabled(Boolean.TRUE)
                    .firstCycle(false)
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
