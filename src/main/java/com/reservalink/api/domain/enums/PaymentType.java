package com.reservalink.api.domain.enums;

public enum PaymentType {
    SUBSCRIPTION,
    FEATURE,
    BOOKING;

    public static PaymentType fromExternalReference(String externalId) {
        if (externalId == null || !externalId.contains("-")) {
            return BOOKING;
        }
        try {
            return PaymentType.valueOf(externalId.substring(0, externalId.indexOf('-')));
        } catch (IllegalArgumentException e) {
            return BOOKING;
        }
    }
}
