package com.reservalink.api.application.service.feature;

import com.reservalink.api.application.output.SubscriptionFeatureRepositoryPort;
import com.reservalink.api.application.output.SubscriptionRepositoryPort;
import com.reservalink.api.domain.FeatureUsage;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.SubscriptionFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeaturePricingServiceImplTest {

    @Mock
    private SubscriptionFeatureRepositoryPort featureRepository;

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @InjectMocks
    private FeaturePricingServiceImpl service;

    @Test
    void shouldReturnZeroWhenFeaturesEmpty() {
        BigDecimal result = service.calculateFeaturesPricing(List.of());
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldReturnFullPriceWhenNotFirstCycle() {
        FeatureUsage usage = FeatureUsage.builder()
                .subscriptionFeatureId("feat1")
                .subscriptionId("sub1")
                .firstCycle(false)
                .build();

        SubscriptionFeature feature = new SubscriptionFeature();
        feature.setId("feat1");
        feature.setPrice(new BigDecimal("20"));

        when(featureRepository.findAllByIds(List.of("feat1"))).thenReturn(List.of(feature));

        BigDecimal result = service.calculateFeaturesPricing(List.of(usage));

        assertEquals(new BigDecimal("20"), result);
    }

    @Test
    void shouldReturnZeroWhenFeatureHasNoPrice() {
        FeatureUsage usage = FeatureUsage.builder()
                .subscriptionFeatureId("feat1")
                .subscriptionId("sub1")
                .firstCycle(false)
                .build();
        SubscriptionFeature feature = new SubscriptionFeature();
        feature.setId("feat1");
        feature.setPrice(null);
        when(featureRepository.findAllByIds(List.of("feat1"))).thenReturn(List.of(feature));

        BigDecimal result = service.calculateFeaturesPricing(List.of(usage));

        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldCalculateProratedPriceActivatedOnFreeTierCycle() {
        FeatureUsage usage = FeatureUsage.builder()
                .subscriptionFeatureId("feat1")
                .subscriptionId("sub1")
                .firstCycle(true)
                .activatedAt(LocalDateTime.of(2026,3,15,22,0,0))
                .build();

        SubscriptionFeature feature = new SubscriptionFeature();
        feature.setId("feat1");
        feature.setPrice(new BigDecimal("100"));

        Subscription subscription = new Subscription();
        subscription.setId("sub1");
        subscription.setExpiration(LocalDate.of(2026,3,24).atStartOfDay());

        when(featureRepository.findAllByIds(List.of("feat1"))).thenReturn(List.of(feature));
        when(subscriptionRepository.findById("sub1")).thenReturn(Optional.of(subscription));

        BigDecimal result = service.calculateFeaturesPricing(List.of(usage));

        assertEquals(new BigDecimal("33.33"), result);
    }

    @Test
    void shouldCalculateProratedPriceMidPeriod() {
        FeatureUsage usage = FeatureUsage.builder()
                .subscriptionFeatureId("feat1")
                .subscriptionId("sub1")
                .firstCycle(true)
                .activatedAt(LocalDateTime.of(2026,3,15,22,0,0))
                .build();

        SubscriptionFeature feature = new SubscriptionFeature();
        feature.setId("feat1");
        feature.setPrice(new BigDecimal("100"));

        Subscription subscription = new Subscription();
        subscription.setId("sub1");
        subscription.setExpiration(LocalDate.of(2026,4,5).atStartOfDay());

        when(featureRepository.findAllByIds(List.of("feat1"))).thenReturn(List.of(feature));
        when(subscriptionRepository.findById("sub1")).thenReturn(Optional.of(subscription));

        BigDecimal result = service.calculateFeaturesPricing(List.of(usage));

        assertEquals(new BigDecimal("73.33"), result);
    }

    @Test
    void shouldChargeOneDayWhenFeatureActivatedOnExpirationDay() {
        FeatureUsage usage = FeatureUsage.builder()
                .subscriptionFeatureId("feat1")
                .subscriptionId("sub1")
                .firstCycle(true)
                .activatedAt(LocalDateTime.of(2026,4,5,10,0))
                .build();

        SubscriptionFeature feature = new SubscriptionFeature();
        feature.setId("feat1");
        feature.setPrice(new BigDecimal("100"));

        Subscription subscription = new Subscription();
        subscription.setId("sub1");
        subscription.setExpiration(LocalDate.of(2026,4,5).atStartOfDay());

        when(featureRepository.findAllByIds(List.of("feat1"))).thenReturn(List.of(feature));
        when(subscriptionRepository.findById("sub1")).thenReturn(Optional.of(subscription));

        BigDecimal result = service.calculateFeaturesPricing(List.of(usage));

        assertEquals(new BigDecimal("3.33"), result);
    }

    @Test
    void shouldReturnFullPriceWhenActivatedBeforeThirtyDaysOfExpiration() {
        FeatureUsage usage = FeatureUsage.builder()
                .subscriptionFeatureId("feat1")
                .subscriptionId("sub1")
                .firstCycle(true)
                .activatedAt(LocalDateTime.of(2026,1,1,10,0))
                .build();

        SubscriptionFeature feature = new SubscriptionFeature();
        feature.setId("feat1");
        feature.setPrice(new BigDecimal("100"));

        Subscription subscription = new Subscription();
        subscription.setId("sub1");
        subscription.setExpiration(LocalDate.of(2026,4,5).atStartOfDay());

        when(featureRepository.findAllByIds(List.of("feat1"))).thenReturn(List.of(feature));
        when(subscriptionRepository.findById("sub1")).thenReturn(Optional.of(subscription));

        BigDecimal result = service.calculateFeaturesPricing(List.of(usage));

        assertEquals(new BigDecimal("100.00"), result);
    }
}