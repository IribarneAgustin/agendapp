package com.reservalink.api.adapter.output.repository.mapper;

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
}