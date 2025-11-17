package com.agendapp.api.repository;

import com.agendapp.api.repository.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, String> {

    @Query("""
    SELECT b
    FROM BookingEntity b
    INNER JOIN b.slotTimeEntity st
    INNER JOIN st.offeringEntity o
    WHERE (
        :clientName IS NULL OR
        LOWER(b.name) LIKE LOWER(CONCAT('%', :clientName, '%'))
    )
      AND (:startDate IS NULL OR st.startDateTime >= :startDate)
      AND (:month IS NULL OR FUNCTION('DATE_FORMAT', st.startDateTime, '%m') = :month)
      AND (:offeringId IS NULL OR o.id = :offeringId)
      AND (o.userEntity.id = :userId)
      AND (b.status IN ('CONFIRMED', 'CANCELLED'))
    ORDER BY st.startDateTime ASC
    """)
    Page<BookingEntity> findBookingGrid(
            @Param("userId") String userId,
            @Param("clientName") String clientName,
            @Param("startDate") LocalDateTime startDate,
            @Param("month") String month,
            @Param("offeringId") String offeringId,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(b)
    FROM BookingEntity b
    INNER JOIN b.slotTimeEntity st
    INNER JOIN st.offeringEntity o
    WHERE o.id = :offeringId
      AND st.startDateTime >= :now
      AND b.status = 'CONFIRMED'
      AND b.enabled = true
      AND st.enabled = true
      AND o.enabled = true
    """)
    Integer getIncomingBookingsCount(@Param("offeringId") String offeringId, @Param("now") LocalDateTime now);

    @Query("""
    SELECT COUNT(b)
    FROM BookingEntity b
    INNER JOIN b.slotTimeEntity st
    WHERE st.id = :slotTimeId
      AND st.startDateTime >= :now
      AND b.status = 'CONFIRMED'
      AND b.enabled = true
      AND st.enabled = true
    """)
    Integer getIncomingBookingsCountBySlotId(@Param("slotTimeId") String slotTimeId, @Param("now") LocalDateTime now);



}
