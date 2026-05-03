package com.reservalink.api.adapter.output.providers;

import com.reservalink.api.application.dto.PaymentCheckoutRequest;
import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.application.output.PaymentGatewayPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MercadoPagoAdapter implements PaymentGatewayPort {

    private final RestTemplate restTemplate;
    private final Environment environment;

    @Value("${api.base.url}")
    private String baseURL;

    @Value("${mercadopago.app.access-token}")
    private String mercadoPagoAppToken;

    @Override
    public String generateCheckoutUrl(PaymentCheckoutRequest request) {
        String url = "https://api.mercadopago.com/checkout/preferences";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getAuthToken() != null ? request.getAuthToken() : mercadoPagoAppToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "items", List.of(Map.of(
                        "title", request.getTitle(),
                        "quantity", 1,
                        "currency_id", request.getCurrency().name(),
                        "unit_price", request.getAmount()
                )),
                "external_reference", request.getExternalId(),
                "notification_url", baseURL + "/payment/mercadopago/webhook",
                "back_urls", Map.of(
                        "success", request.getSuccessURL(),
                        "failure", request.getFailureURL()
                ),
                "auto_return", "approved",
                "metadata", request.getMetadata().toMap()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return environment.acceptsProfiles(Profiles.of("dev")) ? response.getBody().get("sandbox_init_point").toString() : response.getBody().get("init_point").toString();
        } else {
            throw new RuntimeException("Error creating preference");
        }
    }

    @Override
    public PaymentDetails fetchPaymentDetails(String paymentId) {
        String url = "https://api.mercadopago.com/v1/payments/" + paymentId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mercadoPagoAppToken);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new RuntimeException("Empty response from Mercado Pago for payment ID: " + paymentId);
            }

            String status = (String) body.get("status");

            return PaymentDetails.builder()
                    .externalId((String) body.get("external_reference"))
                    .status(status)
                    .approved("approved".equalsIgnoreCase(status))
                    .amount(new BigDecimal(String.valueOf(body.get("transaction_amount"))))
                    .metadata((Map<String, Object>) body.get("metadata"))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch payment details for payment ID: " + paymentId + " from Mercado Pago", e);
        }
    }
}