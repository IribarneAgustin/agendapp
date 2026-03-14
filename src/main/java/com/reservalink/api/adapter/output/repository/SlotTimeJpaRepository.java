package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlotTimeJpaRepository extends JpaRepository<SlotTimeEntity, String> {
    Optional<SlotTimeEntity> findByIdAndEnabledTrue(String id);
}