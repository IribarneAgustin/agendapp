package com.reservalink.api.application.service.payment.webhook;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.application.output.PaymentGatewayPort;
import com.reservalink.api.application.service.payment.strategy.PaymentProcessingStrategy;
import com.reservalink.api.domain.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private final List<PaymentProcessingStrategy> strategies;
    private final PaymentGatewayPort paymentGatewayPort;

    @Override
    public void processWebhook(String paymentId) {
        try {
            PaymentDetails paymentDetails = paymentGatewayPort.getPaymentDetails(paymentId);
            PaymentType type = PaymentType.fromExternalReference(paymentDetails.externalReference());

            strategies.stream()
                    .filter(strategy -> strategy.getType(type))
                    .findFirst()
                    .ifPresentOrElse(
                            strategy -> strategy.process(paymentDetails),
                            () -> log.warn("No strategy found for PaymentType: {}", type)
                    );

        } catch (Exception e) {
            log.error("Unexpected error processing webhook for paymentId: {}", paymentId, e);
            throw new RuntimeException("Webhook processing failed", e);
        }
    }
}