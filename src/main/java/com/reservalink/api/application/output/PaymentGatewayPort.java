package com.reservalink.api.application.output;

import com.reservalink.api.application.dto.PaymentDetails;

public interface PaymentGatewayPort {
    PaymentDetails getPaymentDetails(String paymentId);
}
