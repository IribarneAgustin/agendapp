package com.reservalink.api.application.service.payment;

import com.reservalink.api.adapter.output.repository.PaymentAccountTokenRepository;
import com.reservalink.api.adapter.output.repository.PaymentRepository;
import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.adapter.output.repository.entity.BookingPaymentEntity;
import com.reservalink.api.adapter.output.repository.entity.PaymentAccountTokenEntity;
import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.application.dto.BookingPaymentMetadata;
import com.reservalink.api.application.dto.PaymentCheckoutRequest;
import com.reservalink.api.application.dto.SubscriptionPaymentMetadata;
import com.reservalink.api.application.output.FeatureUsageRepositoryPort;
import com.reservalink.api.application.output.PaymentGatewayPort;
import com.reservalink.api.application.output.SubscriptionPlanRepositoryPort;
import com.reservalink.api.application.service.subscription.SubscriptionPriceService;
import com.reservalink.api.domain.FeatureUsage;
import com.reservalink.api.domain.SubscriptionPlan;
import com.reservalink.api.domain.enums.Currency;
import com.reservalink.api.domain.enums.PaymentStatus;
import com.reservalink.api.domain.enums.PaymentType;
import com.reservalink.api.domain.enums.SubscriptionPlanCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private static final int MIN_RESOURCES = 2;
    private static final int MAX_RESOURCES = 20;

    private final SubscriptionPlanRepositoryPort subscriptionPlanRepositoryPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final SubscriptionPriceService subscriptionPriceService;
    private final FeatureUsageRepositoryPort featureUsageRepositoryPort;
    private final PaymentAccountTokenRepository tokenRepository;
    private final PaymentRepository paymentRepository;

    @Value("${api.base.url}")
    private String baseURL;


    @Override
    public String createSubscriptionCheckoutUrl(String userId, SubscriptionPlanCode planCode, Integer selectedResources) {
        SubscriptionPlan targetPlan = subscriptionPlanRepositoryPort.findByCode(planCode)
                .orElseThrow(() -> new EntityNotFoundException("Subscription Plan not found"));

        if (!isValidResourceCount(planCode, selectedResources)) {
            throw new IllegalArgumentException("Invalid Resources count");
        }

        List<FeatureUsage> premiumFeatures = featureUsageRepositoryPort.findAllAvailableByUserId(userId);
        List<String> premiumFeatureIds = premiumFeatures.stream().map(FeatureUsage::getSubscriptionFeatureId).toList();

        BigDecimal finalPrice = subscriptionPriceService.calculateTotal(targetPlan, selectedResources, premiumFeatures).setScale(2, RoundingMode.HALF_UP);
        String externalId = String.format("%s-%s", PaymentType.SUBSCRIPTION.name(), userId);
        String checkoutTitle = "ReservaLink - Suscripción " + planCode.getDisplayName();

        SubscriptionPaymentMetadata metadata = SubscriptionPaymentMetadata.builder()
                .userId(userId)
                .type(PaymentType.SUBSCRIPTION)
                .premiumFeatureIds(premiumFeatureIds)
                .build();

        PaymentCheckoutRequest request = PaymentCheckoutRequest
                .builder()
                .title(checkoutTitle)
                .amount(finalPrice)
                .currency(Currency.ARS)
                .externalId(externalId)
                .successURL(baseURL + "/public/subscription-payment.html?success=true")
                .failureURL(baseURL + "/public/subscription-payment.html?success=false")
                .metadata(metadata)
                .build();

        return paymentGatewayPort.generateCheckoutUrl(request);
    }

    @Override
    public String createBookingCheckoutURL(BookingEntity bookingEntity, Integer quantity) {
        SlotTimeEntity slotTimeEntity = bookingEntity.getSlotTimeEntity();
        String userId = slotTimeEntity.getOfferingEntity().getUserEntity().getId();
        BigDecimal amountToPaid = BigDecimal.valueOf(slotTimeEntity.getPrice() * (slotTimeEntity.getOfferingEntity().getAdvancePaymentPercentage() / 100.0))
                .setScale(2, RoundingMode.HALF_UP);
        String externalId = UUID.randomUUID().toString();
        PaymentAccountTokenEntity token = tokenRepository.findByUserEntityId(userId).orElseThrow(() -> new RuntimeException("User not linked to MercadoPago"));

        BookingPaymentMetadata metadata = BookingPaymentMetadata.builder()
                .userId(userId)
                .type(PaymentType.BOOKING)
                .build();

        PaymentCheckoutRequest request = PaymentCheckoutRequest
                .builder()
                .title(slotTimeEntity.getOfferingEntity().getName())
                .description("Reserva de turno - " + slotTimeEntity.getOfferingEntity().getName())
                .amount(amountToPaid)
                .currency(Currency.ARS)
                .externalId(externalId)
                .successURL(baseURL + "/public/user-offerings.html?userId=" + userId + "&bookedSuccess=true")
                .failureURL(baseURL + "/public/user-offerings.html?userId=" + userId + "&bookedSuccess=false")
                .authToken(token.getAccessToken())
                .metadata(metadata)
                .build();

        String checkoutURL = paymentGatewayPort.generateCheckoutUrl(request);

        if (checkoutURL == null) {
            throw new RuntimeException("No checkout URL returned from MercadoPago");
        }

        //TODO Consider to remove from here. Return a response with checkoutURL, externalId, and data needed to save this in caller.
        BookingPaymentEntity pending = BookingPaymentEntity.builder()
                .externalId(externalId)
                .enabled(Boolean.TRUE)
                .paymentStatus(PaymentStatus.PENDING)
                .bookingEntity(bookingEntity)
                .amount(amountToPaid.multiply(BigDecimal.valueOf(quantity)))
                .build();
        paymentRepository.save(pending);

        log.info("Booking checkout URL generated successfully");
        return checkoutURL;
    }

    private boolean isValidResourceCount(SubscriptionPlanCode planCode, Integer selectedResources) {
        if (SubscriptionPlanCode.PROFESSIONAL != planCode) {
            return true;
        }
        return selectedResources != null && selectedResources >= MIN_RESOURCES && selectedResources <= MAX_RESOURCES;
    }
}
