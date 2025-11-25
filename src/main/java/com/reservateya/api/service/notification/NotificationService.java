package com.reservateya.api.service.notification;

import com.reservateya.api.repository.entity.BookingEntity;
import com.reservateya.api.repository.entity.UserEntity;

import java.util.List;


public interface NotificationService {
    void sendBookingConfirmed(BookingEntity bookingEntity);

    void sendSubscriptionExpired(List<UserEntity> userEntityList);

    void sendBookingCancelled(BookingEntity bookingEntity);
}
