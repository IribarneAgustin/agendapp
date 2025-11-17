package com.agendapp.api.client;

import com.agendapp.api.controller.request.SubscriptionRequest;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class MercadoPagoClient {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.plan.id}")
    private String planId;

    @Value("${mercadopago.back.url}")
    private String backUrl;

    @Value("${mercadopago.plan.price}")
    private Double planPrice;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            log.info("Mercado Pago SDK initialized successfully.");
        } catch (Exception e) {
            log.error("Error trying to initialize Mercado Pago SDK. Check Access Token.", e);
        }
    }

    public Payment getPaymentDetails(Long paymentId) {
        try {
            PaymentClient client = new PaymentClient();
            return client.get(paymentId);
        } catch (MPException | MPApiException e) {
            log.error("Error trying to get payment details {} on Mercado Pago API.", paymentId, e);
            return null;
        }
    }

    public String createPreapproval(SubscriptionRequest request) {

        String userId = request.getUserId();
        String userEmail = request.getUserEmail();
        String cardToken = request.getCardToken();

        try {
            String url = "https://api.mercadopago.com/preapproval";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> body = Map.of(
                    "reason", "Monthly Subscription - AgendApp",
                    "external_reference", userId,
                    "payer_email", userEmail,
                    "auto_recurring", Map.of(
                            "frequency", 1,
                            "frequency_type", "months",
                            "transaction_amount", planPrice,
                            "currency_id", "ARS"
                    ),
                    "back_url", "https://google.com",
                    "status","authorized"
            );



            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null) {
                log.error("Empty response received from Mercado Pago for user {}", userId);
                throw new IllegalStateException("Empty response from Mercado Pago.");
            }

            // Extract init point
            String initPoint = (String) responseBody.get("init_point");

            if (initPoint == null || initPoint.isEmpty()) {
                log.error("Mercado Pago did not return init_point for user {}. Response: {}", userId, responseBody);
                throw new IllegalStateException("Subscription created, but no init point returned.");
            }

            log.info("Subscription created successfully for user {} - init_point: {}", userId, initPoint);
            return initPoint;

        } catch (HttpClientErrorException e) {
            log.error("HTTP error creating subscription in MP for {}. Response: {}", userId, e.getResponseBodyAsString());
            throw new RuntimeException("Mercado Pago API failure: " + e.getResponseBodyAsString(), e);

        } catch (Exception e) {
            log.error("Unexpected error creating subscription for {}", userId, e);
            throw new RuntimeException("Internal error processing subscription.");
        }
    }

}
