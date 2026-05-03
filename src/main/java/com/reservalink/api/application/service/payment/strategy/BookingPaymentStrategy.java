package com.reservalink.api.application.service.payment.strategy;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.application.output.PaymentRepositoryPort;
import com.reservalink.api.domain.BookingPayment;
import com.reservalink.api.domain.enums.PaymentMethod;
import com.reservalink.api.domain.enums.PaymentStatus;
import com.reservalink.api.domain.enums.PaymentType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingPaymentStrategy implements PaymentProcessor {

    private final PaymentRepositoryPort paymentRepositoryPort;

    @Override
    public PaymentType getType() {
        return PaymentType.BOOKING;
    }

    @Override
    public void process(PaymentDetails details) {
        BookingPayment payment = paymentRepositoryPort.findByExternalId(details.getExternalId())
                .orElseThrow(() -> new EntityNotFoundException(details.getExternalId()));
        if (details.isApproved()) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaymentMethod(PaymentMethod.MERCADO_PAGO);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }
        paymentRepositoryPort.save(payment);
    }
}