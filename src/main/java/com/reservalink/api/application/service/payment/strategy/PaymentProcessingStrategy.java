package com.reservalink.api.application.service.payment.strategy;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.domain.PaymentType;

public interface PaymentProcessingStrategy {
    boolean getType(PaymentType paymentType);

    void process(PaymentDetails paymentDetails);
}