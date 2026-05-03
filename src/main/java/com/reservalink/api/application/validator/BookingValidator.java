package com.reservalink.api.application.validator;

import com.reservalink.api.adapter.input.controller.request.BookingRequest;
import com.reservalink.api.application.output.BookingRepositoryPort;
import com.reservalink.api.application.output.SlotTimeRepositoryPort;
import com.reservalink.api.application.output.UserRepositoryPort;
import com.reservalink.api.application.service.subscription.SubscriptionUsageService;
import com.reservalink.api.domain.SlotTime;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.enums.FeatureName;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final SlotTimeRepositoryPort slotTimeRepositoryPort;
    private final SubscriptionUsageService subscriptionUsageService;
    private final BookingRepositoryPort bookingRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public void validate(BookingRequest bookingRequest) {
        SlotTime slotTime = slotTimeRepositoryPort.findById(bookingRequest.getSlotTimeId().toString())
                .orElseThrow(() -> new IllegalArgumentException("The SlotTimeId: " + bookingRequest.getSlotTimeId() + " does not exists"));

        boolean isResourceBusy = bookingRepositoryPort.existsOverlappingBookingForResource(
                slotTime.getResourceId(),
                slotTime.getOffering().getId(),
                slotTime.getEndDateTime(),
                slotTime.getStartDateTime()
        );

        if (isResourceBusy) {
            throw new BusinessRuleException(BusinessErrorCodes.RESOURCE_NOT_AVAILABLE.name());
        }

        Integer newCapacity = slotTime.getCapacityAvailable() - bookingRequest.getQuantity();
        if (newCapacity < 0) {
            log.warn("No capacity available for slotTimeId: {}. Booking creation ignored for {}", bookingRequest.getSlotTimeId(), bookingRequest.getEmail());
            throw new BusinessRuleException(BusinessErrorCodes.NO_CAPACITY_AVAILABLE.name());
        }

        Subscription subscription = userRepositoryPort.findUserSubscriptionByUserId(slotTime.getOffering().getUserId())
                .orElseThrow(EntityNotFoundException::new);

        boolean featureAvailable = subscriptionUsageService.canConsume(subscription.getId(), subscription.getSubscriptionPlanId(), FeatureName.BOOKINGS);
        if (!featureAvailable) {
            throw new BusinessRuleException(BusinessErrorCodes.FEATURE_LIMIT_EXCEEDED.name());
        }
    }
}
