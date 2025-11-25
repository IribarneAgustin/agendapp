package com.reservateya.api.repository;

import com.reservateya.api.repository.entity.SlotTimeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface SlotTimeRepository extends JpaRepository<SlotTimeEntity, String> {
    Page<SlotTimeEntity> findAllByOfferingEntityIdAndEnabledTrueAndEndDateTimeGreaterThanEqualOrderByStartDateTimeAsc(
            String offeringId,
            LocalDateTime now,
            Pageable pageable
    );

    List<SlotTimeEntity> findByOfferingEntityIdAndEnabledTrue(String string);

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

}
