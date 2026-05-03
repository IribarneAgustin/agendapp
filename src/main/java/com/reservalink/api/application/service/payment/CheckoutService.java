package com.reservalink.api.application.service.payment;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.domain.enums.SubscriptionPlanCode;

public interface CheckoutService {
    String createSubscriptionCheckoutUrl(String userId, SubscriptionPlanCode planCode, Integer selectedResources);

    String createBookingCheckoutURL(BookingEntity bookingEntitySaved, Integer quantity);
}