package com.reservalink.api.adapter.output.repository.mapper;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionPlanEntity;
import com.reservalink.api.domain.SubscriptionPlan;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPlanMapper {

    public SubscriptionPlan toDomain(SubscriptionPlanEntity entity) {
        if (entity == null) {
            return null;
        }

        return SubscriptionPlan.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .price(entity.getPrice())
                .maxBookings(entity.getMaxBookings())
                .maxResources(entity.getMaxResources())
                .enabled(entity.getEnabled())
                .build();
    }

    public SubscriptionPlanEntity toEntity(SubscriptionPlan domain) {
        if (domain == null) {
            return null;
        }

        return SubscriptionPlanEntity.builder()
                .id(domain.getId() != null ? domain.getId() : null)
                .code(domain.getCode())
                .price(domain.getPrice())
                .maxBookings(domain.getMaxBookings())
                .maxResources(domain.getMaxResources())
                .enabled(domain.getEnabled())
                .build();
    }

}