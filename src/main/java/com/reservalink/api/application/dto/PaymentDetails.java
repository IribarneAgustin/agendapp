package com.reservalink.api.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class PaymentDetails {
    private final String externalId;
    private final String status;
    private final boolean approved;
    private final BigDecimal amount;
    private final Map<String, Object> metadata;
}