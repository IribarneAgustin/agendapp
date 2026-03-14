package com.reservalink.api.adapter.output.repository.mapper;

import com.reservalink.api.adapter.output.repository.entity.OfferingEntity;
import com.reservalink.api.domain.Offering;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OfferingRepositoryMapper {

    public Offering toDomain(OfferingEntity entity) {
        if (entity == null) {
            return null;
        }
        return Offering.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .capacity(entity.getCapacity())
                .advancePaymentPercentage(entity.getAdvancePaymentPercentage())
                .status(entity.getStatus())
                .termsAndConditions(entity.getTermsAndConditions())
                .userId(entity.getUserEntity() != null ? entity.getUserEntity().getId() : null)
                .enabled(entity.getEnabled())
                .build();
    }
}