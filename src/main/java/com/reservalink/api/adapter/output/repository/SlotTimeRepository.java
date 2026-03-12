package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface SlotTimeRepository extends JpaRepository<SlotTimeEntity, String> {
    Page<SlotTimeEntity> findAllByOfferingEntityIdAndResourceEntityIdAndEnabledTrueAndEndDateTimeGreaterThanEqualOrderByStartDateTimeAsc(
            String offeringId,
            String resourceId,
            LocalDateTime now,
            Pageable pageable
    );

    List<SlotTimeEntity> findByOfferingEntityIdAndEnabledTrue(String offeringId);

    @Query("""
            SELECT COUNT(s) > 0 FROM SlotTimeEntity s
            WHERE s.offeringEntity.id = :offeringId
              AND s.enabled = true
              AND s.startDateTime < :endDateTime
              AND s.endDateTime > :startDateTime
              AND s.id <> :slotTimeId
            """)
    boolean existsOverlappingSlot(@Param("offeringId") String offeringId,
                                  @Param("slotTimeId") String slotTimeId,
                                  @Param("startDateTime") LocalDateTime startDateTime,
                                  @Param("endDateTime") LocalDateTime endDateTime);

    @Query("""
                SELECT st
                FROM SlotTimeEntity st
                WHERE st.offeringEntity.id = :offeringId
                  AND st.resourceEntity.id = :resourceId
                  AND st.enabled = true
                  AND st.endDateTime >= :now
                  AND st.capacityAvailable > 0
                  AND NOT EXISTS (
                      SELECT b
                      FROM BookingEntity b
                      WHERE b.slotTimeEntity.resourceEntity.id = :resourceId
                        AND b.status = 'CONFIRMED'
                        AND b.slotTimeEntity.offeringEntity.id <> :offeringId
                        AND b.slotTimeEntity.startDateTime < st.endDateTime
                        AND b.slotTimeEntity.endDateTime > st.startDateTime
                  )
                ORDER BY st.startDateTime ASC
            """)
    Page<SlotTimeEntity> findAllAvailableSlotTimesByOfferingAndResourceId(
            @Param("offeringId") String offeringId,
            @Param("resourceId") String resourceId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    List<SlotTimeEntity> findByOfferingEntityIdAndResourceEntityIdAndEnabledTrue(String offeringId, String resourceId);

    @Modifying
    @Query("""
                UPDATE SlotTimeEntity st
                SET st.enabled = false
                WHERE st.resourceEntity.id = :resourceId
                  AND st.enabled = true
            """)
    void deleteByResourceId(@Param("resourceId") String resourceId);
}
