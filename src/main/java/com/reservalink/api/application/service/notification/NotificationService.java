package com.reservalink.api.application.service.notification;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionPaymentEntity;
import com.reservalink.api.adapter.output.repository.entity.UserEntity;
import com.reservalink.api.domain.Booking;

import java.util.List;
import java.util.Map;


public interface NotificationService {
    void sendBookingConfirmed(BookingEntity bookingEntity);

    void sendSubscriptionExpired(List<UserEntity> userEntityList);

    void sendBookingCancelled(BookingEntity bookingEntity);

    void sendSubscriptionPayment(SubscriptionPaymentEntity subscriptionPaymentEntity, UserEntity user);

    void sendAboutToExpire(Map<Integer, List<UserEntity>> notificationsMap);

    void sendRecoverExpired(Map<Integer, List<UserEntity>> notificationsMap);

    void sendBookingReminder(Booking booking, NotificationChannel channel);

    void sendResetPasswordRequest(String userEmail, String rawToken);

    void sendNewUserRegistered(UserEntity userEntity);
}
