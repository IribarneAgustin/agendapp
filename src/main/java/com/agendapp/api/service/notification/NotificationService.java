package com.agendapp.api.service.notification;

import com.agendapp.api.repository.entity.BookingEntity;
import com.agendapp.api.repository.entity.UserEntity;

import java.util.List;


public interface NotificationService {
    void sendBookingConfirmed(BookingEntity bookingEntity);

    void sendSubscriptionExpired(List<UserEntity> userEntityList);
}
