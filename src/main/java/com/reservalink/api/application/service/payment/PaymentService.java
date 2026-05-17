package com.reservalink.api.application.service.payment;

public interface PaymentService {

    void processPaymentWebhook(String paymentId);
}
