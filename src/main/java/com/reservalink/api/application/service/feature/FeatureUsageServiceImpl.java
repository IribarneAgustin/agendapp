package com.reservalink.api.application.service.feature;

import com.reservalink.api.adapter.input.controller.request.FeatureUsageRequest;
import com.reservalink.api.application.dto.FeatureUsageDetail;
import com.reservalink.api.application.dto.FeatureUsageResponse;
import com.reservalink.api.application.output.FeatureUsageRepositoryPort;
import com.reservalink.api.application.output.SubscriptionFeatureRepositoryPort;
import com.reservalink.api.application.output.SubscriptionPlanRepositoryPort;
import com.reservalink.api.application.output.SubscriptionRepositoryPort;
import com.reservalink.api.application.output.UserRepositoryPort;
import com.reservalink.api.application.service.payment.CheckoutService;
import com.reservalink.api.application.service.payment.PaymentService;
import com.reservalink.api.domain.FeatureUsage;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.SubscriptionFeature;
import com.reservalink.api.domain.SubscriptionPlan;
import com.reservalink.api.domain.enums.FeatureStatus;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureUsageServiceImpl implements FeatureUsageService {

    private final SubscriptionFeatureRepositoryPort subscriptionFeatureRepository;
    private final UserRepositoryPort userRepositoryPort;
    private final PaymentService paymentService;
    private final FeatureUsageRepositoryPort featureUsageRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final CheckoutService checkoutService;
    private final SubscriptionPlanRepositoryPort subscriptionPlanRepositoryPort;


    @Override
    public FeatureUsageResponse create(FeatureUsageRequest request, String userId) {
        SubscriptionFeature subscriptionFeature = subscriptionFeatureRepository.findByNameAndUsageLimit(request.name(), request.usageLimit())
                .orElseThrow(() -> new BusinessRuleException(BusinessErrorCodes.FEATURE_NOT_ENABLED.name()));

        Subscription subscription = userRepositoryPort.findUserSubscriptionByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found for user id " + userId));

        FeatureUsage newFeatureUsage = FeatureUsage.builder()
                .subscriptionFeatureId(subscriptionFeature.getId())
                .subscriptionId(subscription.getId())
                .featureStatus(FeatureStatus.PENDING)
                .enabled(true)
                .firstCycle(false)
                .usage(0)
                .build();

        FeatureUsage savedFeatureUsage = featureUsageRepositoryPort.create(newFeatureUsage);

        String checkoutURL = paymentService.createPremiumFeatureCheckoutURL(subscriptionFeature, savedFeatureUsage.getId());

        return FeatureUsageResponse.builder()
                .featureUsage(savedFeatureUsage)
                .premiumFeatureCheckoutURL(checkoutURL)
                .build();
    }

    @Override
    public void delete(String featureUsageId, String userId) {
        FeatureUsage featureUsage = featureUsageRepositoryPort
                .findById(featureUsageId).orElseThrow(() -> new IllegalArgumentException("Feature usage not found for id " + featureUsageId));
        Subscription subscription = userRepositoryPort.findUserSubscriptionByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found for user id " + userId));

        if (!featureUsage.getSubscriptionId().equals(subscription.getId())) {
            throw new BusinessRuleException(BusinessErrorCodes.UNAUTHORIZED_RESOURCE_ACCESS.name());
        }
        featureUsage.setEnabled(false);
        featureUsage.setFeatureStatus(FeatureStatus.DELETED);
        featureUsageRepositoryPort.update(featureUsage);

        SubscriptionPlan currentPlan = subscriptionPlanRepositoryPort.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new EntityNotFoundException("Subscription plan not found"));
        String newSubscriptionCheckoutURL = checkoutService.createSubscriptionCheckoutUrl(userId, currentPlan.getCode(), subscription.getSelectedResourcesLimit());
        subscription.setCheckoutLink(newSubscriptionCheckoutURL);
        subscriptionRepositoryPort.update(subscription);
    }

    @Override
    public List<FeatureUsageDetail> findAllAvailableByUserId(String userId) {
        List<FeatureUsage> featureUsageList = featureUsageRepositoryPort.findAllAvailableByUserId(userId);

        if (featureUsageList.isEmpty()) {
            return List.of();
        }

        List<String> subscriptionFeatureIds = featureUsageList.stream()
                .map(FeatureUsage::getSubscriptionFeatureId)
                .distinct()
                .toList();

        List<SubscriptionFeature> subscriptionFeatures =
                subscriptionFeatureRepository.findAllByIds(subscriptionFeatureIds);

        Map<String, SubscriptionFeature> subscriptionFeatureMap =
                subscriptionFeatures.stream()
                        .collect(Collectors.toMap(SubscriptionFeature::getId, sf -> sf));

        return featureUsageList.stream()
                .map(featureUsage -> {

                    SubscriptionFeature subscriptionFeature =
                            subscriptionFeatureMap.get(featureUsage.getSubscriptionFeatureId());

                    return FeatureUsageDetail.builder()
                            .id(featureUsage.getId())
                            .enabled(featureUsage.getEnabled())
                            .featureStatus(featureUsage.getFeatureStatus())
                            .usage(featureUsage.getUsage())
                            .usageLimit(String.valueOf(subscriptionFeature.getUsageLimit()))
                            .build();
                })
                .toList();
    }

}