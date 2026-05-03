package com.reservalink.api.domain;

import com.reservalink.api.domain.enums.PeriodStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionUsage {
    private String subscriptionId;
    private Integer bookingUsage;
    private LocalDateTime startPeriodDateTime;
    private PeriodStatus periodStatus;
    private Boolean enabled;
}