package com.agendapp.api.service.payment;

import com.agendapp.api.repository.entity.BookingEntity;
import com.agendapp.api.repository.entity.SlotTimeEntity;
import com.mercadopago.resources.payment.Payment;

import java.util.Map;

public interface PaymentService {
    //FIXME decouple Payment object for a generic DTO to make it abstract
    void processPaymentWebhook(Payment paymentDetails);

    String createCheckoutLink(String email, String userId);

    String createBookingCheckoutURL(BookingEntity bookingEntity, Integer quantity);

    String processBookingWebhook(String paymentId);
}
