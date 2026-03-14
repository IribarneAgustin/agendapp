package com.reservalink.api.domain;

public enum BookingReminderJobStatus {
    PENDING,
    SENT,
    RETRY,
    CANCELLED,
    FAILED,
    DELETED
}