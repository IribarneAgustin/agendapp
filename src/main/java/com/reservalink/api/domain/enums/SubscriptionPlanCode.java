package com.reservalink.api.domain.enums;

import lombok.Getter;

@Getter
public enum SubscriptionPlanCode {
    FREE_TIER("Plan de Prueba Gratis"),
    BASIC("Plan Basic"),
    PROFESSIONAL("Plan Profesional");

    private final String displayName;

    SubscriptionPlanCode(String displayName) {
        this.displayName = displayName;
    }
}