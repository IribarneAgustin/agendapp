package com.reservalink.api.adapter.output.repository.mapper;

import com.reservalink.api.adapter.output.repository.entity.OfferingEntity;
import com.reservalink.api.adapter.output.repository.entity.PackageSessionEntity;
import com.reservalink.api.domain.PackageSession;
import org.springframework.stereotype.Component;

@Component
public class PackageSessionPersistenceMapper {

    public PackageSession toDomain(PackageSessionEntity entity) {
        if (entity == null) return null;

        return PackageSession.builder()
                .id(entity.getId())
                .offeringId(entity.getOfferingEntity() != null ? entity.getOfferingEntity().getId() : null)
                .offeringName(entity.getOfferingEntity() != null ? entity.getOfferingEntity().getName() : null)
                .userId(entity.getOfferingEntity() != null && entity.getOfferingEntity().getUserEntity() != null ? entity.getOfferingEntity().getUserEntity().getId() : null)
                .advancePaymentPercentage(entity.getOfferingEntity() != null ? entity.getOfferingEntity().getAdvancePaymentPercentage() : null)
                .sessionLimit(entity.getSessionLimit())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .enabled(entity.getEnabled())
                .build();
    }

    public PackageSessionEntity toEntity(PackageSession domain) {
        if (domain == null) return null;

        OfferingEntity offeringEntity = OfferingEntity.builder()
                .id(domain.getOfferingId())
                .build();

        PackageSessionEntity entity = PackageSessionEntity.builder()
                .offeringEntity(offeringEntity)
                .sessionLimit(domain.getSessionLimit())
                .price(domain.getPrice())
                .status(domain.getStatus())
                .enabled(domain.isEnabled())
                .build();

        entity.setId(domain.getId());
        return entity;
    }
}