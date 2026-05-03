package com.reservalink.api.application.service.payment.strategy;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.domain.enums.PaymentType;

public interface PaymentProcessor {
    void process(PaymentDetails details);

    PaymentType getType();
}