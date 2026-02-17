package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.FeatureUsageEntity;
import com.reservalink.api.domain.FeatureName;
import com.reservalink.api.domain.FeatureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureUsageJpaRepository extends JpaRepository<FeatureUsageEntity, String> {
    Optional<FeatureUsageEntity> findByEnabledTrueAndSubscriptionFeatureEntity_IdAndSubscriptionEntity_IdAndFeatureStatus(String subscriptionFeatureId, String userSubscriptionId, FeatureStatus featureStatus);

    Optional<FeatureUsageEntity> findByIdAndEnabledTrue(String featureUsageId);

    @Query("""
                SELECT fu
                FROM FeatureUsageEntity fu
                JOIN fu.subscriptionFeatureEntity sf
                WHERE fu.enabled = true
                  AND sf.enabled = true
                  AND fu.featureStatus = 'ACTIVE'
                  AND fu.subscriptionEntity.id = :subscriptionId
                  AND sf.name = :featureName
                  AND (fu.usage + 1 <= sf.usageLimit)
                ORDER BY fu.creationTimestamp DESC
            """)
    List<FeatureUsageEntity> findLatestActiveAvailable(
            @Param("subscriptionId") String subscriptionId,
            @Param("featureName") FeatureName featureName
    );

    @Query("""
       SELECT fu
       FROM FeatureUsageEntity fu
       JOIN fu.subscriptionEntity s
       JOIN UserEntity u ON u.subscriptionEntity.id = s.id
       WHERE fu.enabled = true
         AND fu.featureStatus = 'ACTIVE'
         AND u.id = :userId
       """)
    List<FeatureUsageEntity> findAllActiveByUserId(@Param("userId") String userId);

}