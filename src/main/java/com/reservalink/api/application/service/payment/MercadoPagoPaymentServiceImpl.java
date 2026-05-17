package com.reservalink.api.application.service.payment;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.application.output.PaymentGatewayPort;
import com.reservalink.api.application.output.PaymentRepositoryPort;
import com.reservalink.api.application.service.payment.strategy.PaymentProcessor;
import com.reservalink.api.domain.enums.PaymentStatus;
import com.reservalink.api.domain.enums.PaymentType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoPaymentServiceImpl implements PaymentService {

    private final PaymentGatewayPort paymentGatewayPort;
    private final List<PaymentProcessor> processorList;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private Map<PaymentType, PaymentProcessor> paymentProcessors;

    @PostConstruct
    private void init() {
        paymentProcessors = processorList.stream()
                .collect(Collectors.toMap(PaymentProcessor::getType, Function.identity()));
    }

    @Override
    public void processPaymentWebhook(String paymentId) {
        try {
            PaymentDetails details = paymentGatewayPort.fetchPaymentDetails(paymentId);
            String externalId = details.getExternalId();
            PaymentType type = PaymentType.fromExternalReference(externalId);
            PaymentProcessor paymentProcessor = paymentProcessors.get(type);

            if (paymentRepositoryPort.existsByExternalIdAndPaymentStatus(externalId, PaymentStatus.COMPLETED)) {
                log.info("Payment with externalId {} was already successfully processed. Webhook Skipped.", externalId);
                return;
            }

            if (paymentProcessor != null) {
                paymentProcessor.process(details);
                log.info("Payment {} processed successfully as {}", paymentId, type);
            } else {
                log.warn("No processor found for payment type: {}. Skipping logic.", type);
            }

        } catch (Exception e) {
            log.error("Unexpected error processing webhook for payment {}: {}", paymentId, e.getMessage(), e);
        }
    }
}
