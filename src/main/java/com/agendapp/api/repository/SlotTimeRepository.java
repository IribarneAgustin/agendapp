package com.agendapp.api.repository;

import com.agendapp.api.entity.SlotTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface SlotTimeRepository extends JpaRepository<SlotTime, String> {
    Page<SlotTime> findAllByOfferingIdAndActiveTrueAndEndDateTimeGreaterThanEqualOrderByStartDateTimeAsc(
            String offeringId,
            LocalDateTime now,
            Pageable pageable
    );

    List<SlotTime> findByOfferingIdAndActiveTrue(String string);

    @Query("""
    SELECT COUNT(s) > 0 FROM SlotTime s
    WHERE s.offering.id = :offeringId
      AND s.active = true
      AND s.startDateTime < :endDateTime
      AND s.endDateTime > :startDateTime
      AND s.id <> :slotTimeId
    """)
    boolean existsOverlappingSlot(@Param("offeringId") String offeringId,
                                  @Param("slotTimeId") String slotTimeId,
                                  @Param("startDateTime") LocalDateTime startDateTime,
                                  @Param("endDateTime") LocalDateTime endDateTime);

}
