package com.reservalink.api.adapter.output.repository.mapper;

import com.reservalink.api.adapter.output.repository.entity.OfferingCategoryEntity;
import com.reservalink.api.adapter.output.repository.entity.OfferingEntity;
import com.reservalink.api.adapter.output.repository.entity.UserEntity;
import com.reservalink.api.domain.Offering;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                .categoryId(entity.getCategory().getId())
                .build();
    }

    public List<Offering> toDomain(List<OfferingEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<OfferingEntity> toEntity(List<Offering> domains) {
        return domains.stream()
                .map(this::toEntity)
                .toList();
    }

    public OfferingEntity toEntity(Offering domain) {
        if (domain == null) return null;

        return OfferingEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .capacity(domain.getCapacity())
                .advancePaymentPercentage(domain.getAdvancePaymentPercentage())
                .status(domain.getStatus())
                .termsAndConditions(domain.getTermsAndConditions())
                .enabled(domain.isEnabled())
                .userEntity(UserEntity.builder().id(domain.getUserId()).build())
                .category(OfferingCategoryEntity.builder().id(domain.getCategoryId()).build())
                .build();
    }
}