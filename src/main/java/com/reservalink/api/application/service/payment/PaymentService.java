package com.reservalink.api.application.service.payment;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.domain.BookingPackage;
import com.reservalink.api.domain.FeatureUsage;
import com.reservalink.api.domain.PackageSession;
import com.reservalink.api.domain.SubscriptionFeature;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    String createSubscriptionCheckoutURL(String userId, List<FeatureUsage> features);

    String createBookingCheckoutURL(BookingEntity bookingEntity, Integer quantity);

    String processPaymentWebhook(String paymentId);

    String createPremiumFeatureCheckoutURL(SubscriptionFeature subscriptionFeature, String featureUsageId);

    String createPackageCheckoutURL(BookingPackage bookingPackage, PackageSession packageSession);

}
