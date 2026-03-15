package com.reservalink.api.application.service.payment;

import com.reservalink.api.adapter.output.repository.PaymentAccountTokenRepository;
import com.reservalink.api.adapter.output.repository.PaymentRepository;
import com.reservalink.api.adapter.output.repository.UserRepository;
import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.adapter.output.repository.entity.BookingPaymentEntity;
import com.reservalink.api.adapter.output.repository.entity.PaymentAccountTokenEntity;
import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionPaymentEntity;
import com.reservalink.api.adapter.output.repository.entity.UserEntity;
import com.reservalink.api.application.output.FeatureUsageRepositoryPort;
import com.reservalink.api.application.output.SubscriptionFeatureRepositoryPort;
import com.reservalink.api.application.output.SubscriptionRepositoryPort;
import com.reservalink.api.application.output.UserRepositoryPort;
import com.reservalink.api.application.service.feature.FeatureLifecycleService;
import com.reservalink.api.application.service.feature.FeaturePricingService;
import com.reservalink.api.application.service.notification.NotificationService;
import com.reservalink.api.domain.FeatureName;
import com.reservalink.api.domain.FeatureStatus;
import com.reservalink.api.domain.FeatureUsage;
import com.reservalink.api.domain.PaymentMethod;
import com.reservalink.api.domain.PaymentStatus;
import com.reservalink.api.domain.PaymentType;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.SubscriptionFeature;
import com.reservalink.api.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoPaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final PaymentAccountTokenRepository tokenRepository;
    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final FeatureLifecycleService featureLifecycleService;
    private final FeatureUsageRepositoryPort featureUsageRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final SubscriptionFeatureRepositoryPort subscriptionFeatureRepositoryPort;
    private final Environment environment;
    private final FeaturePricingService featurePricingService;

    @Value("${api.base.url}")
    private String baseURL;

    @Value("${mercadopago.app.access-token}")
    private String mercadoPagoAppToken;

    @Value("${app.subscription.price}")
    private BigDecimal subscriptionPrice;

    @Override
    public String createSubscriptionCheckoutURL(String userId, List<FeatureUsage> features) {
        String url = "https://api.mercadopago.com/checkout/preferences";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mercadoPagoAppToken);

        String externalId = "SUBSCRIPTION-" + userId;
        BigDecimal featuresPricing = featurePricingService.calculateFeaturesPricing(features);

        Map<String, Object> body = Map.of(
                "items", new Object[]{
                        Map.of(
                                "title", "Suscripción Mensual ReservaLink",
                                "quantity", 1,
                                "currency_id", "ARS",
                                "unit_price", subscriptionPrice.add(featuresPricing).setScale(2, RoundingMode.HALF_UP)
                        )
                },
                "external_reference", externalId,
                "notification_url", baseURL + "/payment/mercadopago/webhook",
                "back_urls", Map.of(
                        "success", baseURL + "/public/subscription-payment.html?success=true",
                        "failure", baseURL + "/public/subscription-payment.html?success=false"
                ),
                "auto_return", "approved",
                "metadata", Map.of(
                        "premiumFeatures", features.isEmpty() ? Collections.emptyList() : features.stream()
                                .map(FeatureUsage::getSubscriptionFeatureId)
                                .toList())
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return environment.acceptsProfiles(Profiles.of("dev")) ? response.getBody().get("sandbox_init_point").toString() : response.getBody().get("init_point").toString();
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
                "items", new Object[]{
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

            switch (PaymentType.fromExternalReference(externalId)) {
                case SUBSCRIPTION -> processSubscriptionPayment(externalId, approved, payment);
                case FEATURE -> processFeaturePayment(externalId, approved);
                case BOOKING -> processBookingPayment(externalId, approved);
            }

        } catch (Exception e) {
            log.error("Unexpected error processing booking webhook: {}", e.getMessage());
        }
        return externalId;
    }

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
            if (subscriptionEntity.getExpiration().isBefore(LocalDateTime.now())) {//TODO Consider use paymentDate from payment map
                subscriptionEntity.setExpiration(LocalDateTime.now().plusMonths(1));
            } else {
                subscriptionEntity.setExpiration(subscriptionEntity.getExpiration().plusMonths(1));
            }
            subscriptionEntity.setExpired(false);

            List<String> premiumFeatureIds = extractPremiumFeatureIds(payment);
            if (!premiumFeatureIds.isEmpty()) {
                featureLifecycleService.renew(premiumFeatureIds, subscriptionEntity.getId());
                List<FeatureUsage> featureUsagesAvailable = featureUsageRepositoryPort.findAllAvailableByUserId(userId);
                String updatedCheckoutURL = createSubscriptionCheckoutURL(userId, featureUsagesAvailable);
                subscriptionEntity.setCheckoutLink(updatedCheckoutURL);
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

    private void processFeaturePayment(String externalId, boolean approved) {
        String featureUsageId = externalId.replace("FEATURE-", "");
        if (approved) {
            log.info("Processing payment for FeatureUsage id {}", featureUsageId);
            FeatureUsage featureUsage = featureUsageRepositoryPort.findById(featureUsageId)
                    .orElseThrow(() -> new IllegalArgumentException("Feature Usage Id not found: " + featureUsageId));

            //TODO: when more features premium be added, refactor to handle the name correctly. (fetch from their repo)
            featureUsageRepositoryPort.findByUserSubscriptionIdAndFeatureNameAndStatus(featureUsage.getSubscriptionId(), FeatureName.WHATSAPP_NOTIFICATIONS, FeatureStatus.ACTIVE)
                    .ifPresent(activeFeature -> {
                        activeFeature.setFeatureStatus(FeatureStatus.EXCHANGED);
                        featureUsageRepositoryPort.update(activeFeature);
                    });

            featureUsage.setFeatureStatus(FeatureStatus.ACTIVE);
            featureUsage.setFirstCycle(true);
            featureUsage.setActivatedAt(LocalDateTime.now());
            featureUsageRepositoryPort.update(featureUsage);

            String userSubscriptionId = featureUsage.getSubscriptionId();
            Subscription userSubscription = subscriptionRepositoryPort.findById(userSubscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("Subscription Id not found: " + userSubscriptionId));

            User user = userRepositoryPort.findBySubscriptionId(userSubscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found for subscription id: " + userSubscriptionId));

            String newSubscriptionCheckoutURL = createSubscriptionCheckoutURL(user.getId(), List.of(featureUsage));
            userSubscription.setCheckoutLink(newSubscriptionCheckoutURL);

            subscriptionRepositoryPort.update(userSubscription);
            log.info("Payment processed successfully");
        } else {
            log.info("Feature payment failed for Feature Usage id {}", featureUsageId);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractPremiumFeatureIds(Map<String, Object> payment) {
        Map<String, Object> metadata =
                (Map<String, Object>) payment.getOrDefault("metadata", Map.of());

        return Optional.ofNullable(metadata.get("premium_features"))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .map(list -> list.stream().map(String::valueOf).toList())
                .orElse(Collections.emptyList());
    }

}
