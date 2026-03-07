package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.PackageSessionEntity;
import com.reservalink.api.application.output.PackageSessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PackageSessionRepositoryAdapter implements PackageSessionRepositoryPort {

    private final PackageSessionJpaRepository jpaRepository;

    @Override
    public PackageSessionEntity save(PackageSessionEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<PackageSessionEntity> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<PackageSessionEntity> findByOfferingId(String offeringId) {
        return jpaRepository.findByOfferingEntityId(offeringId);
    }
}
