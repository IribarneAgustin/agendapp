package com.reservalink.api.adapter.output.repository.mapper;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionUsageEntity;
import com.reservalink.api.domain.SubscriptionUsage;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionUsageRepositoryMapper {

    public SubscriptionUsageEntity toEntity(SubscriptionUsage domain) {
        if (domain == null) {
            return null;
        }

        return SubscriptionUsageEntity.builder()
                .subscription(SubscriptionEntity.builder().id(domain.getSubscriptionId()).build())
                .bookingUsage(domain.getBookingUsage())
                .startPeriodDateTime(domain.getStartPeriodDateTime())
                .periodStatus(domain.getPeriodStatus())
                .enabled(domain.getEnabled())
                .build();
    }

    public SubscriptionUsage toDomain(SubscriptionUsageEntity entity) {
        if (entity == null) {
            return null;
        }
        return SubscriptionUsage.builder()
                .subscriptionId(entity.getSubscription().getId())
                .bookingUsage(entity.getBookingUsage())
                .startPeriodDateTime(entity.getStartPeriodDateTime())
                .periodStatus(entity.getPeriodStatus())
                .enabled(entity.getEnabled())
                .build();
    }
}