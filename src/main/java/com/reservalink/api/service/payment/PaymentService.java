package com.reservalink.api.service.payment;

import com.reservalink.api.repository.entity.BookingEntity;
import com.mercadopago.resources.payment.Payment;

public interface PaymentService {
    String createSubscriptionCheckoutURL(String userId);

    String createBookingCheckoutURL(BookingEntity bookingEntity, Integer quantity);

    String processPaymentWebhook(String paymentId);

}
