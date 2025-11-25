package com.reservateya.api.service.notification;

import lombok.Getter;

@Getter
public enum NotificationMotive {
    SUBSCRIPTION_EXPIRED("subscription_expired", "Tu suscripción ha expirado"),
    BOOKING_CONFIRMED("booking_confirmed", "Tu reserva ha sido confirmada"),
    ADMIN_BOOKING_CONFIRMED("admin_booking_confirmed", "Recibiste una nueva reserva!"),
    BOOKING_CANCELLED("booking_cancelled", "Tu reserva ha sido cancelada"),
    ADMIN_BOOKING_CANCELLED("admin_booking_cancelled", "Reserva cancelada");

    private final String templateName;
    private final String subject;

    NotificationMotive(String templateName, String subject) {
        this.templateName = templateName;
        this.subject = subject;
    }
}
