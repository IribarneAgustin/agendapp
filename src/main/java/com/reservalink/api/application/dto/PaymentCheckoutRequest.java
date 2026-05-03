package com.reservalink.api.application.dto;

import com.reservalink.api.domain.enums.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentCheckoutRequest {
    private final String title;
    private final String description;
    private final BigDecimal amount;
    private final Currency currency;
    private final String externalId;
    private final String successURL;
    private final String failureURL;
    private final String authToken;
    private final PaymentMetadata metadata;
}