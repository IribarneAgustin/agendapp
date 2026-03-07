package com.reservalink.api.application.output;

import com.reservalink.api.adapter.output.repository.entity.PackageSessionEntity;

import java.util.Optional;

public interface PackageSessionRepositoryPort {
    PackageSessionEntity save(PackageSessionEntity entity);

    Optional<PackageSessionEntity> findById(String id);

    Optional<PackageSessionEntity> findByOfferingId(String offeringId);
}
