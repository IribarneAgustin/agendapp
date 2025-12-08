package com.reservalink.api.controller.request;

public enum MpWebhookType {
    PAYMENT("payment"),
    SUBSCRIPTION_PREAPPROVAL("subscription_preapproval"),
    UNKNOWN("unknown");

    private final String value;

    MpWebhookType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static MpWebhookType from(String raw) {
        if (raw == null) return UNKNOWN;
        for (MpWebhookType t : values()) {
            if (t.value.equalsIgnoreCase(raw)) {
                return t;
            }
        }
        return UNKNOWN;
    }
}
