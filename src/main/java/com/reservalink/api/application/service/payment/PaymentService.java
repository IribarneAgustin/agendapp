package com.reservalink.api.application.service.payment;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.domain.PremiumFeature;
import com.reservalink.api.domain.SubscriptionFeature;

import java.util.List;

public interface PaymentService {
    String createSubscriptionCheckoutURL(String userId, List<SubscriptionFeature> features);

    String createBookingCheckoutURL(BookingEntity bookingEntity, Integer quantity);

    String processPaymentWebhook(String paymentId);

    String createPremiumFeatureCheckoutURL(String subscriptionFeatureId, PremiumFeature premiumFeature);

}
