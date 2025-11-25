package com.reservateya.api.repository;

import com.reservateya.api.repository.entity.OfferingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferingRepository extends JpaRepository<OfferingEntity, String> {
    List<OfferingEntity> findByUserEntityIdAndEnabledTrue(String userId);
}
