package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.application.output.SlotTimeRepositoryPort;
import com.reservalink.api.domain.SlotTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SlotTimeRepositoryAdapter implements SlotTimeRepositoryPort {

    private final SlotTimeJpaRepository jpaRepository;

    @Override
    public Optional<SlotTime> findById(String id) {
        return jpaRepository.findByIdAndEnabledTrue(id).map(this::toDomain);
    }

    @Override
    public void save(SlotTime domain) {
        SlotTimeEntity entity = jpaRepository.findByIdAndEnabledTrue(domain.getId())
                .orElseGet(() -> SlotTimeEntity.builder().id(domain.getId()).build());

        entity.setStartDateTime(domain.getStartDateTime());
        entity.setEndDateTime(domain.getEndDateTime());
        entity.setPrice(domain.getPrice());
        entity.setCapacityAvailable(domain.getCapacityAvailable());
        entity.setMaxCapacity(domain.getMaxCapacity());
        entity.setEnabled(domain.isEnabled());

        jpaRepository.save(entity);
    }

    private SlotTime toDomain(SlotTimeEntity entity) {
        return SlotTime.builder()
                .id(entity.getId())
                .offeringId(entity.getOfferingEntity().getId())
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