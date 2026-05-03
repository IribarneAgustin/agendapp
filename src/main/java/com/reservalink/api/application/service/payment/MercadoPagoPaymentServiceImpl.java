package com.reservalink.api.application.service.payment;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.application.output.PaymentGatewayPort;
import com.reservalink.api.application.service.booking.BookingService;
import com.reservalink.api.application.service.payment.strategy.PaymentProcessor;
import com.reservalink.api.domain.SubscriptionFeature;
import com.reservalink.api.domain.enums.FeatureName;
import com.reservalink.api.domain.enums.PaymentType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoPaymentServiceImpl implements PaymentService {

    private final RestTemplate restTemplate;
    private final Environment environment;
    private final PaymentGatewayPort paymentGatewayPort;
    private final List<PaymentProcessor> processorList;
    private Map<PaymentType, PaymentProcessor> paymentProcessors;
    private final BookingService bookingService;

    @Value("${api.base.url}")
    private String baseURL;

    @Value("${mercadopago.app.access-token}")
    private String mercadoPagoAppToken;

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

            if (paymentProcessor != null) {
                paymentProcessor.process(details);
                log.info("Payment {} processed successfully as {}", paymentId, type);
                if (type == PaymentType.BOOKING) {
                    bookingService.confirmBooking(externalId);
                }
            } else {
                log.warn("No processor found for payment type: {}. Skipping logic.", type);
            }

        } catch (Exception e) {
            log.error("Unexpected error processing webhook for payment {}: {}", paymentId, e.getMessage(), e);
        }
    }

    //TODO: Move to CheckoutService
    @Override
    public String createPremiumFeatureCheckoutURL(SubscriptionFeature subscriptionFeature, String featureUsageId) {
        String url = "https://api.mercadopago.com/checkout/preferences";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mercadoPagoAppToken);

        String externalId = "FEATURE-" + featureUsageId;
        String title = "Funcionalidad Premium";
        if (FeatureName.WHATSAPP_NOTIFICATIONS == subscriptionFeature.getName()) {
            title = "Paquete de " + subscriptionFeature.getUsageLimit() + " recordatorios por WhatsApp";
        }

        Map<String, Object> body = Map.of(
                "items", new Object[]{
                        Map.of(
                                "title", title,
                                "quantity", 1,
                                "currency_id", "ARS",
                                "unit_price", subscriptionFeature.getPrice()
                        )
                },
                "external_reference", externalId,
                "notification_url", baseURL + "/payment/mercadopago/webhook",
                "back_urls", Map.of(
                        "success", baseURL + "/public/payment-status.html?success=true",
                        "failure", baseURL + "/public/payment-status.html?success=false"
                ),
                "auto_return", "approved"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return environment.acceptsProfiles(Profiles.of("dev")) ? response.getBody().get("sandbox_init_point").toString() : response.getBody().get("init_point").toString();
        } else {
            throw new RuntimeException("Error creating preference");
        }
    }
}
