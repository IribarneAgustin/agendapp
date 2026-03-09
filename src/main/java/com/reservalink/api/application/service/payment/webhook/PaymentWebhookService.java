package com.reservalink.api.application.service.payment.webhook;

public interface PaymentWebhookService {
    void processWebhook(String paymentId);
}
