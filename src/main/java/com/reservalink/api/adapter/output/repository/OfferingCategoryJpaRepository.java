package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.OfferingCategoryEntity;
import org.antlr.v4.runtime.misc.MultiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferingCategoryJpaRepository extends JpaRepository<OfferingCategoryEntity, String> {
    List<OfferingCategoryEntity> findAllByUserEntity_IdAndEnabledTrue(String id);

    OfferingCategoryEntity findByIdAndEnabledTrue(String categoryId);

    OfferingCategoryEntity findByUserEntity_IdAndIsDefaultTrueAndEnabledTrue(String userId);

    OfferingCategoryEntity findByUserEntity_IdAndNameAndEnabledTrue(String userId, String name);
}
