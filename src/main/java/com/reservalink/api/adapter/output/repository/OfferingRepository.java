package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.OfferingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferingRepository extends JpaRepository<OfferingEntity, String> {
    List<OfferingEntity> findByUserEntityIdAndEnabledTrue(String userId);

    List<OfferingEntity> findAllByEnabledTrueAndCategoryId(String categoryId);
}
