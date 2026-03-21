package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.OfferingEntity;
import com.reservalink.api.adapter.output.repository.mapper.OfferingRepositoryMapper;
import com.reservalink.api.application.output.OfferingRepositoryPort;
import com.reservalink.api.domain.Offering;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OfferingRepositoryAdapter implements OfferingRepositoryPort {

    private final OfferingRepository offeringJpaRepository;
    private final OfferingRepositoryMapper offeringRepositoryMapper;

    @Override
    public List<Offering> findAllByCategoryId(String categoryId) {
        return offeringRepositoryMapper.toDomain(offeringJpaRepository.findAllByEnabledTrueAndCategoryId(categoryId));
    }

    @Override
    public void saveAll(List<Offering> offeringList) {
        List<OfferingEntity> entities = offeringRepositoryMapper.toEntity(offeringList);
        offeringJpaRepository.saveAll(entities);
    }

    @Override
    public Offering save(Offering offering) {
        OfferingEntity entity = offeringRepositoryMapper.toEntity(offering);
        OfferingEntity saved = offeringJpaRepository.save(entity);
        return offeringRepositoryMapper.toDomain(saved);
    }

    @Override
    public Optional<Offering> findById(String id) {
        return offeringJpaRepository.findById(id).map(offeringRepositoryMapper::toDomain);
    }

    @Override
    public List<Offering> findByUserId(String userId) {
        return offeringJpaRepository.findByUserEntityIdAndEnabledTrue(userId)
                .stream()
                .map(offeringRepositoryMapper::toDomain)
                .toList();
    }

}
