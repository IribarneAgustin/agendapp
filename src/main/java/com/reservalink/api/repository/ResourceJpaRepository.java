package com.reservalink.api.repository;

import com.reservalink.api.repository.entity.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceJpaRepository extends JpaRepository<ResourceEntity, String> {
    List<ResourceEntity> findAllByEnabledTrueAndUserEntity_Id(String userId);

    @Query("""
                SELECT DISTINCT re
                FROM ResourceEntity re
                JOIN SlotTimeEntity st ON st.resourceEntity = re
                WHERE re.userEntity.id = :userId
                  AND st.offeringEntity.id = :offeringId
                  AND st.enabled = true
                  AND re.enabled = true
                  AND st.capacityAvailable <> 0
            """)
    List<ResourceEntity> findAllByEnabledTrueAndUserIdAndOfferingId(String userId, String offeringId);
}