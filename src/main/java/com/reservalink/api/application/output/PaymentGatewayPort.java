package com.reservalink.api.application.output;

import com.reservalink.api.application.dto.PaymentCheckoutRequest;
import com.reservalink.api.application.dto.PaymentDetails;

public interface PaymentGatewayPort {
    String generateCheckoutUrl(PaymentCheckoutRequest request);

    PaymentDetails fetchPaymentDetails(String paymentId);
}