package com.reservalink.api.adapter.output.repository.mapper;

import com.reservalink.api.adapter.output.repository.entity.OfferingEntity;
import com.reservalink.api.adapter.output.repository.entity.ResourceEntity;
import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.domain.SlotTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlotTimeRepositoryMapper {

    private final OfferingRepositoryMapper offeringRepositoryMapper;

    public SlotTime toDomain(SlotTimeEntity entity) {
        if (entity == null) {
            return null;
        }
        return SlotTime.builder()
                .id(entity.getId())
                .offering(offeringRepositoryMapper.toDomain(entity.getOfferingEntity()))
                .resourceId(entity.getResourceEntity().getId())
                .startDateTime(entity.getStartDateTime())
                .endDateTime(entity.getEndDateTime())
                .price(entity.getPrice())
                .capacityAvailable(entity.getCapacityAvailable())
                .maxCapacity(entity.getMaxCapacity())
                .enabled(entity.getEnabled())
                .build();
    }

    public SlotTimeEntity toEntity(SlotTime domain) {
        if (domain == null) {
            return null;
        }

        SlotTimeEntity entity = new SlotTimeEntity();

        entity.setId(domain.getId());
        entity.setStartDateTime(domain.getStartDateTime());
        entity.setEndDateTime(domain.getEndDateTime());
        entity.setPrice(domain.getPrice());
        entity.setCapacityAvailable(domain.getCapacityAvailable());
        entity.setMaxCapacity(domain.getMaxCapacity());
        entity.setEnabled(domain.getEnabled());
        if (domain.getOffering() != null) {
            entity.setOfferingEntity(
                    OfferingEntity.builder()
                            .id(domain.getOffering().getId())
                            .build()
            );
        }

        if (domain.getResourceId() != null) {
            entity.setResourceEntity(
                    ResourceEntity.builder()
                            .id(domain.getResourceId())
                            .build()
            );
        }

        return entity;
    }
}