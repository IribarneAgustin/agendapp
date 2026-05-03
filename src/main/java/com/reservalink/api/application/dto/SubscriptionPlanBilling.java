package com.reservalink.api.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionPlanBilling {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean expired;
}
