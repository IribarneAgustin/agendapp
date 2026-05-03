package com.reservalink.api.domain;

import com.reservalink.api.domain.enums.FeatureName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionFeature {
    private String id;
    private Integer usageLimit;
    private FeatureName name;
    private Boolean enabled;
    private BigDecimal price;
}