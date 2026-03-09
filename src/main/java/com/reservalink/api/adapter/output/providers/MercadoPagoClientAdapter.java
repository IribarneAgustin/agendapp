package com.reservalink.api.adapter.output.providers;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.application.output.PaymentGatewayPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MercadoPagoClientAdapter implements PaymentGatewayPort {

    private final RestTemplate restTemplate;

    @Value("${mercadopago.app.access-token}")
    private String mercadoPagoAppToken;

    @Override
    public PaymentDetails getPaymentDetails(String paymentId) {
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

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                return new PaymentDetails(
                        paymentId,
                        String.valueOf(body.get("external_reference")),
                        String.valueOf(body.get("status"))
                );
            }
            throw new RuntimeException("MercadoPago API error: " + response.getStatusCode());

        } catch (Exception e) {
            log.error("Failed to fetch payment details from MercadoPago for ID: {}", paymentId, e);
            throw e;
        }
    }
}