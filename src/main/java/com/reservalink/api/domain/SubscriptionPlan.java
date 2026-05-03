package com.reservalink.api.domain;


import com.reservalink.api.domain.enums.SubscriptionPlanCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionPlan {
    private String id;
    private SubscriptionPlanCode code;
    private BigDecimal price;
    private Integer maxBookings;
    private Integer maxResources;
    private Boolean enabled;
}