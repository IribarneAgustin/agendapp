package com.reservalink.api.application.service.payment;

import com.reservalink.api.domain.SubscriptionFeature;

public interface PaymentService {

    void processPaymentWebhook(String paymentId);

    String createPremiumFeatureCheckoutURL(SubscriptionFeature subscriptionFeature, String featureUsageId);

}
