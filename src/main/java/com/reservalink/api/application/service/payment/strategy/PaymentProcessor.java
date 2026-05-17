package com.reservalink.api.application.service.payment.strategy;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.domain.enums.PaymentType;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentProcessor {

    @Transactional(rollbackFor = Exception.class)
    void process(PaymentDetails details);

    PaymentType getType();
}