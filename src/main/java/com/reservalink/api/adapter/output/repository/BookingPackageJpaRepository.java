package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BookingPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingPackageJpaRepository extends JpaRepository<BookingPackageEntity, String> {
    Optional<BookingPackageEntity> findByIdAndEnabledTrue(String id);
}
