package com.agendapp.api.service.payment;

import com.agendapp.api.client.MercadoPagoClient;
import com.agendapp.api.exception.BusinessErrorCodes;
import com.agendapp.api.exception.BusinessRuleException;
import com.agendapp.api.repository.BookingRepository;
import com.agendapp.api.repository.PaymentAccountTokenRepository;
import com.agendapp.api.repository.PaymentRepository;
import com.agendapp.api.repository.SlotTimeRepository;
import com.agendapp.api.repository.entity.BookingEntity;
import com.agendapp.api.repository.entity.BookingPaymentEntity;
import com.agendapp.api.repository.entity.BookingStatus;
import com.agendapp.api.repository.entity.PaymentAccountTokenEntity;
import com.agendapp.api.repository.entity.PaymentMethod;
import com.agendapp.api.repository.entity.PaymentStatus;
import com.agendapp.api.repository.entity.SlotTimeEntity;
import com.agendapp.api.repository.entity.SubscriptionEntity;
import com.agendapp.api.repository.entity.UserEntity;
import com.agendapp.api.repository.UserRepository;
import com.agendapp.api.service.booking.BookingService;
import com.mercadopago.resources.payment.Payment;
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


    @Value("${api.base.url}")
    private String baseURL;

    @Value("${mercadopago.app.access-token}")
    private String mercadoPagoAppToken;


    public MercadoPagoPaymentServiceImpl(UserRepository userRepository, PaymentAccountTokenRepository tokenRepository, RestTemplate restTemplate, PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.restTemplate = restTemplate;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void processPaymentWebhook(Payment paymentDetails) {
       /* String userId = paymentDetails.getExternalReference();
        if (userId == null || userId.isEmpty()) {
            log.warn("Webhook received without external_reference");
            return;
        }

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(RuntimeException::new);
        SubscriptionEntity subscriptionEntity = userEntity.getSubscriptionEntity();

        String status = paymentDetails.getStatus();
        LocalDateTime now = LocalDateTime.now();

        if ("approved".equalsIgnoreCase(status)) {
            log.info("Pago APROBADO recibido para el usuario: {}. ID de Pago MP: {}", userId, paymentDetails.getId());

            LocalDateTime nextExpiration = now.plusMonths(1);
            subscriptionEntity.setExpired(false);
            subscriptionEntity.setExpiration(nextExpiration);

        } else if ("cancelled".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status)) {
            log.warn("Pago CANCELADO/RECHAZADO para el usuario: {}. ID de Pago MP: {}", userId, paymentDetails.getId());

            subscriptionEntity.setExpired(true);
        } else {
            log.info("Pago en estado {} para el usuario: {}. No se requiere acción de DB inmediata.", status, userId);
            return;
        }
        userEntity.setSubscriptionEntity(subscriptionEntity);

        userRepository.save(userEntity);
        log.info("Estado de suscripción actualizado para el usuario: {}", userId);*/
    }

    @Override
    public String createCheckoutLink(String email, String userId) {
        try {
            return null;//mercadoPagoClient.createPreapproval(email, userId);
        } catch (Exception e) {
            log.error("Error generating Mercado Pago checkout link", e);
            return null;
        }
    }

    @Override
    public String createBookingCheckoutURL(BookingEntity bookingEntity, Integer quantity) {
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
    public String processBookingWebhook(String paymentId) {
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

            String finalExternalId = externalId;
            BookingPaymentEntity bookingPaymentEntity = paymentRepository.findByExternalId(externalId)
                    .orElseThrow(() -> new RuntimeException("Payment not found by externalId: " + finalExternalId));

            if ("approved".equalsIgnoreCase(status)) {
                bookingPaymentEntity.setPaymentStatus(PaymentStatus.COMPLETED);
                bookingPaymentEntity.setPaymentMethod(PaymentMethod.MERCADO_PAGO);
            } else {
                bookingPaymentEntity.setPaymentStatus(PaymentStatus.FAILED);
            }
            paymentRepository.saveAndFlush(bookingPaymentEntity);

        } catch (Exception e) {
            log.error("Unexpected error processing booking webhook: {}", e.getMessage());
        }
        return externalId;
    }


}
