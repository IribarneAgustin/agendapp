package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.adapter.output.repository.mapper.SlotTimeRepositoryMapper;
import com.reservalink.api.application.output.SlotTimeRepositoryPort;
import com.reservalink.api.domain.SlotTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SlotTimeRepositoryAdapter implements SlotTimeRepositoryPort {

    private final SlotTimeJpaRepository jpaRepository;
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

}