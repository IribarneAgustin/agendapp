package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.PackageSessionEntity;
import com.reservalink.api.adapter.output.repository.mapper.PackageSessionPersistenceMapper;
import com.reservalink.api.application.output.PackageSessionRepositoryPort;
import com.reservalink.api.domain.PackageSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PackageSessionRepositoryAdapter implements PackageSessionRepositoryPort {

    private final PackageSessionJpaRepository jpaRepository;
    private final PackageSessionPersistenceMapper mapper;

    @Override
    public PackageSession save(PackageSession domain) {
        PackageSessionEntity entityToSave = mapper.toEntity(domain);
        PackageSessionEntity savedEntity = jpaRepository.save(entityToSave);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PackageSession> findById(String id) {
        return jpaRepository.findByIdAndEnabledTrue(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PackageSession> findByOfferingId(String offeringId) {
        return jpaRepository.findByOfferingEntityIdAndEnabledTrue(offeringId).map(mapper::toDomain);
    }
}
