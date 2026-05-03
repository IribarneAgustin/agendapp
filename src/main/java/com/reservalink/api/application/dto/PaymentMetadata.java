package com.reservalink.api.application.dto;

import com.reservalink.api.domain.enums.PaymentType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@Getter
@SuperBuilder
public abstract class PaymentMetadata {
    private final PaymentType type;
    private final String userId;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("payment_type", type.name());
        map.put("user_id", userId);
        return map;
    }
}