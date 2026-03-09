package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlotTimeJpaRepository extends JpaRepository<SlotTimeEntity, String> {
    Optional<SlotTimeEntity> findByIdAndEnabledTrue(String id);
}
