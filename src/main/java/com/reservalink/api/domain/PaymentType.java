package com.reservalink.api.domain;

public enum PaymentType {
    SUBSCRIPTION,
    FEATURE,
    BOOKING,
    PACKAGE;

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
