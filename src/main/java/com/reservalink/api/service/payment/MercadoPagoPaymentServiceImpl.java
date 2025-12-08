package com.reservalink.api.service.payment;

import com.reservalink.api.repository.PaymentAccountTokenRepository;
import com.reservalink.api.repository.PaymentRepository;
import com.reservalink.api.repository.entity.BookingEntity;
import com.reservalink.api.repository.entity.BookingPaymentEntity;
import com.reservalink.api.repository.entity.PaymentAccountTokenEntity;
import com.reservalink.api.repository.entity.PaymentMethod;
import com.reservalink.api.repository.entity.PaymentStatus;
import com.reservalink.api.repository.entity.SlotTimeEntity;
import com.reservalink.api.repository.UserRepository;
import com.reservalink.api.repository.entity.SubscriptionEntity;
import com.reservalink.api.repository.entity.SubscriptionPaymentEntity;
import com.reservalink.api.repository.entity.UserEntity;
import com.reservalink.api.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MercadoPagoPaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;

    private final PaymentAccountTokenRepository tokenRepository;

    private final RestTemplate restTemplate;

    private final PaymentRepository paymentRepository;

    private final NotificationService notificationService;

    @Value("${api.base.url}")
    private String baseURL;

    @Value("${mercadopago.app.access-token}")
    private String mercadoPagoAppToken;

    @Value("${app.subscription.price}")
    private Double subscriptionPrice;


    public MercadoPagoPaymentServiceImpl(UserRepository userRepository, PaymentAccountTokenRepository tokenRepository, RestTemplate restTemplate, PaymentRepository paymentRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.restTemplate = restTemplate;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
    }

    @Override
    public String createSubscriptionCheckoutURL(String userId) {
        String url = "https://api.mercadopago.com/checkout/preferences";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mercadoPagoAppToken);

        String externalId = "SUBSCRIPTION-" + userId;

        Map<String, Object> body = Map.of(
                "items", new Object[]{
                        Map.of(
                                "title", "Suscripción Mensual ReservaLink",
                                "quantity", 1,
                                "currency_id", "ARS",
                                "unit_price", subscriptionPrice
                        )
                },
                "external_reference", externalId,
                "back_urls", Map.of(
                        "success", baseURL + "/public/subscription-payment.html?success=true",
                        "failure", baseURL + "/public/subscription-payment.html?success=false"
                ),
                "auto_return", "approved"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().get("init_point").toString();
        } else {
            throw new RuntimeException("Error creating subscription preference");
        }
    }


    @Override
    public String createBookingCheckoutURL(BookingEntity bookingEntity, Integer quantity) {
        log.info("Creating booking checkout URL");
        SlotTimeEntity slotTimeEntity = bookingEntity.getSlotTimeEntity();
        String userId = slotTimeEntity.getOfferingEntity().getUserEntity().getId();
        PaymentAccountTokenEntity token = tokenRepository.findByUserEntityId(userId)
                .orElseThrow(() -> new RuntimeException("User not linked to MercadoPago"));

        String accessToken = token.getAccessToken();
        String url = "https://api.mercadopago.com/checkout/preferences";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        Double amountToPaid = slotTimeEntity.getPrice() * (slotTimeEntity.getOfferingEntity().getAdvancePaymentPercentage() / 100.0);
        String externalId = UUID.randomUUID().toString();

        Map<String, Object> preferenceBody = Map.of(
                "items", new Object[] {
                        Map.of(
                                "title", slotTimeEntity.getOfferingEntity().getName(),
                                "description", "Reserva de turno - " + slotTimeEntity.getOfferingEntity().getName(),
                                "quantity", quantity,
                                "currency_id", "ARS",
                                "unit_price", amountToPaid
                        )
                },
                "external_reference", externalId,
                "back_urls", Map.of(
                        "success", baseURL + "/public/user-offerings.html?userId=" + userId + "&bookedSuccess=true",
                        "failure", baseURL + "/public/user-offerings.html?userId=" + userId + "&bookedSuccess=false"
                ),
                "auto_return", "approved"
        );


        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(preferenceBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object checkoutUrl = response.getBody().get("init_point");

                BookingPaymentEntity pending = BookingPaymentEntity.builder()
                        .externalId(externalId)
                        .enabled(Boolean.TRUE)
                        .paymentStatus(PaymentStatus.PENDING)
                        .bookingEntity(bookingEntity)
                        .amount(new BigDecimal(amountToPaid * quantity))
                        .build();
                paymentRepository.save(pending);

                if (checkoutUrl != null) {
                    log.info("Booking checkout URL generated successfully");
                    return checkoutUrl.toString();
                } else {
                    throw new RuntimeException("No checkout URL returned from MercadoPago");
                }
            } else {
                throw new RuntimeException("Error creating preference: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error creating MercadoPago preference", e);
            throw new RuntimeException("Failed to create MercadoPago preference", e);
        }
    }

    @Override
    public String processPaymentWebhook(String paymentId) {
        String externalId = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(mercadoPagoAppToken);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.mercadopago.com/v1/payments/" + paymentId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> payment = response.getBody();
            externalId = (String) payment.get("external_reference");
            String status = (String) payment.get("status");
            boolean approved = "approved".equalsIgnoreCase(status);

            if (externalId.startsWith("SUBSCRIPTION-")) {
                processSubscriptionPayment(externalId, approved, payment);
            } else {
                processBookingPayment(externalId, approved);
            }

        } catch (Exception e) {
            log.error("Unexpected error processing booking webhook: {}", e.getMessage());
        }
        return externalId;
    }

    private void processBookingPayment(String externalId, boolean approved) {
        BookingPaymentEntity bookingPaymentEntity = paymentRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Payment not found by externalId: " + externalId));
        if (approved) {
            bookingPaymentEntity.setPaymentStatus(PaymentStatus.COMPLETED);
            bookingPaymentEntity.setPaymentMethod(PaymentMethod.MERCADO_PAGO);
        } else {
            bookingPaymentEntity.setPaymentStatus(PaymentStatus.FAILED);
        }
        paymentRepository.saveAndFlush(bookingPaymentEntity);
    }

    private void processSubscriptionPayment(String externalId, boolean approved, Map<String, Object> payment) {
        String userId = externalId.replace("SUBSCRIPTION-", "");
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + userId));

        SubscriptionEntity subscriptionEntity = user.getSubscriptionEntity();
        if (approved) {
            if(subscriptionEntity.getExpiration().isBefore(LocalDateTime.now())) {//TODO Consider use paymentDate from payment map
                subscriptionEntity.setExpiration(LocalDateTime.now().plusMonths(1));
            } else {
                subscriptionEntity.setExpiration(subscriptionEntity.getExpiration().plusMonths(1));
            }
        }
        SubscriptionPaymentEntity subscriptionPayment = SubscriptionPaymentEntity.builder()
                .subscriptionEntity(user.getSubscriptionEntity())
                .externalId(externalId)
                .paymentStatus(approved ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                .paymentMethod(PaymentMethod.MERCADO_PAGO)
                .amount(new BigDecimal(payment.get("transaction_amount").toString()))
                .paymentDate(LocalDateTime.now())
                .build();
        paymentRepository.saveAndFlush(subscriptionPayment);
        notificationService.sendSubscriptionPayment(subscriptionPayment, user);
    }


}
