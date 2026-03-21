package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.adapter.output.repository.mapper.SlotTimeRepositoryMapper;
import com.reservalink.api.application.output.SlotTimeRepositoryPort;
import com.reservalink.api.domain.SlotTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SlotTimeRepositoryAdapter implements SlotTimeRepositoryPort {

    private final SlotTimeRepository jpaRepository;
    private final SlotTimeRepositoryMapper mapper;

    @Override
    public Optional<SlotTime> findById(String id) {
        return jpaRepository.findByIdAndEnabledTrue(id).map(mapper::toDomain);
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
        entity.setEnabled(domain.getEnabled());

        jpaRepository.save(entity);
    }

    @Override
    public List<SlotTime> findByOfferingEntityIdAndEnabledTrue(String offeringId) {
        return jpaRepository.findByOfferingEntityIdAndEnabledTrue(offeringId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public void saveAll(List<SlotTime> slots) {
        List<SlotTimeEntity> entities = slots.stream().map(mapper::toEntity).toList();
        jpaRepository.saveAll(entities);
    }

}