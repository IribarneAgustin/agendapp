package com.reservalink.api.service.notification;

import com.reservalink.api.repository.entity.BookingEntity;
import com.reservalink.api.repository.entity.SubscriptionPaymentEntity;
import com.reservalink.api.repository.entity.UserEntity;

import java.util.List;
import java.util.Map;


public interface NotificationService {
    void sendBookingConfirmed(BookingEntity bookingEntity);

    void sendSubscriptionExpired(List<UserEntity> userEntityList);

    void sendBookingCancelled(BookingEntity bookingEntity);

    void sendSubscriptionPayment(SubscriptionPaymentEntity subscriptionPaymentEntity, UserEntity user);

    void sendAboutToExpire(Map<Integer, List<UserEntity>> notificationsMap);

    void sendRecoverExpired(Map<Integer, List<UserEntity>> notificationsMap);

    void sendBookingReminder(List<BookingEntity> incomingBookingList);

    void sendResetPasswordRequest(String userEmail, String userId);

    void sendNewUserRegistered(UserEntity userEntity);
}
