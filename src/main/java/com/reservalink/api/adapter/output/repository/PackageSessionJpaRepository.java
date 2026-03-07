package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.PackageSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageSessionJpaRepository extends JpaRepository<PackageSessionEntity, String> {
    Optional<PackageSessionEntity> findByOfferingEntityId(String offeringId);
}
