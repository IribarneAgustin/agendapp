package com.reservalink.api.service.payment;

import com.reservalink.api.repository.entity.BookingEntity;
import com.mercadopago.resources.payment.Payment;

public interface PaymentService {
    //FIXME decouple Payment object for a generic DTO to make it abstract
    void processPaymentWebhook(Payment paymentDetails);

    String createCheckoutLink(String email, String userId);

    String createBookingCheckoutURL(BookingEntity bookingEntity, Integer quantity);

    String processBookingWebhook(String paymentId);
}
