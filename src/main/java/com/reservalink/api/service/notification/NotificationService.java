package com.reservalink.api.service.notification;

import com.reservalink.api.repository.entity.BookingEntity;
import com.reservalink.api.repository.entity.UserEntity;

import java.util.List;


public interface NotificationService {
    void sendBookingConfirmed(BookingEntity bookingEntity);

    void sendSubscriptionExpired(List<UserEntity> userEntityList);

    void sendBookingCancelled(BookingEntity bookingEntity);
}
