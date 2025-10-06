package com.agendapp.api.repository;

import com.agendapp.api.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    @Query("""
    SELECT b
    FROM Booking b
    INNER JOIN b.slotTime st
    INNER JOIN st.offering o
    WHERE (
        :clientName IS NULL OR
        LOWER(b.name) LIKE LOWER(CONCAT('%', :clientName, '%'))
    )
      AND (:startDate IS NULL OR st.startDateTime >= :startDate)
      AND (:month IS NULL OR FUNCTION('DATE_FORMAT', st.startDateTime, '%m') = :month)
      AND (:offeringId IS NULL OR o.id = :offeringId)
    ORDER BY st.startDateTime ASC
    """)
    Page<Booking> findBookingGrid(
            @Param("clientName") String clientName,
            @Param("startDate") LocalDateTime startDate,
            @Param("month") String month,
            @Param("offeringId") String offeringId,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(b)
    FROM Booking b
    INNER JOIN b.slotTime st
    INNER JOIN st.offering o
    WHERE o.id = :offeringId
      AND st.startDateTime >= :now
      AND b.status = 'CONFIRMED'
      AND b.active = true
      AND st.active = true
      AND o.active = true
    """)
    Integer getIncomingBookingsCount(@Param("offeringId") String offeringId, @Param("now") LocalDateTime now);

    @Query("""
    SELECT COUNT(b)
    FROM Booking b
    INNER JOIN b.slotTime st
    WHERE st.id = :slotTimeId
      AND st.startDateTime >= :now
      AND b.status = 'CONFIRMED'
      AND b.active = true
      AND st.active = true
    """)
    Integer getIncomingBookingsCountBySlotId(@Param("slotTimeId") String slotTimeId, @Param("now") LocalDateTime now);



}
