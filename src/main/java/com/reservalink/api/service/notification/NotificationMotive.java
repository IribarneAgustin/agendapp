package com.reservalink.api.service.notification;

import lombok.Getter;

@Getter
public enum NotificationMotive {
    SUBSCRIPTION_EXPIRED("subscription_expired", "Tu suscripción ha expirado"),
    BOOKING_CONFIRMED("booking_confirmed", "Tu reserva ha sido confirmada"),
    ADMIN_BOOKING_CONFIRMED("admin_booking_confirmed", "Recibiste una nueva reserva!"),
    BOOKING_CANCELLED("booking_cancelled", "Tu reserva ha sido cancelada"),
    ADMIN_BOOKING_CANCELLED("admin_booking_cancelled", "Reserva cancelada"),
    SUBSCRIPTION_PAYMENT_SUCCESS("subscription_payment", "Pago de suscripción recibido"),
    SUBSCRIPTION_PAYMENT_FAILED("subscription_payment", "Problema con el pago de tu suscripción"),
    SUBSCRIPTION_ABOUT_TO_EXPIRE("subscription_about_to_expire", "Tu suscripción está por vencer"),
    SUBSCRIPTION_EXPIRED_RECOVER("subscription_expired_recover", "Recuperá tu cuenta"),
    BOOKING_REMINDER("booking_reminder", "Recordá que tenes una reserva para mañana"),
    RESET_PASSWORD("reset_password", "Recuperá tu contraseña"),
    NEW_USER_REGISTERED("welcome_user", "¡Bienvenido a ReservaLink!"),
    APP_NEW_USER_REGISTERED("welcome_user", "¡Nuevo usuario registrado en ReservaLink!");

    private final String templateName;
    private final String subject;

    NotificationMotive(String templateName, String subject) {
        this.templateName = templateName;
        this.subject = subject;
    }
}
