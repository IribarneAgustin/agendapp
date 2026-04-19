package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, String> {

    @Query("""
            SELECT b
            FROM BookingEntity b
            INNER JOIN b.slotTimeEntity st
            INNER JOIN st.offeringEntity o
            INNER JOIN st.resourceEntity r
            WHERE (
                :clientName IS NULL OR
                LOWER(b.name) LIKE LOWER(CONCAT('%', :clientName, '%'))
            )
              AND (:startDate IS NULL OR DATE(st.startDateTime) = :startDate)
              AND (:fromDate IS NULL OR DATE(st.startDateTime) >= :fromDate)
              AND (:month IS NULL OR FUNCTION('DATE_FORMAT', st.startDateTime, '%m') = :month)
              AND (:offeringId IS NULL OR o.id = :offeringId)
              AND (o.userEntity.id = :userId)
              AND (b.status IN ('CONFIRMED', 'CANCELLED'))
              AND (:resourceId IS NULL OR r.id = :resourceId )
            ORDER BY st.startDateTime DESC
            """)
    Page<BookingEntity> findBookingGrid(
            @Param("userId") String userId,
            @Param("clientName") String clientName,
            @Param("startDate") LocalDate startDate,
            @Param("month") String month,
            @Param("offeringId") String offeringId,
            @Param("resourceId") String resourceId,
            @Param("fromDate") LocalDate fromDate,
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

    @Query("""
                SELECT COUNT(b.id) > 0
                FROM BookingEntity b
                JOIN b.slotTimeEntity st
                WHERE st.resourceEntity.id = :resourceId
                  AND b.status = 'CONFIRMED'
                  AND st.startDateTime < :newEnd
                  AND st.endDateTime > :newStart
                  AND st.offeringEntity.id <> :offeringId
            """)
    boolean existsOverlappingBookingForResource(
            @Param("resourceId") String resourceId,
            @Param("offeringId") String offeringId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd
    );


    @Query("""
            SELECT COUNT(b)
            FROM BookingEntity b
            INNER JOIN b.slotTimeEntity st
            WHERE st.resourceEntity.id = :resourceId
              AND st.endDateTime >= :now
              AND st.enabled = true
              AND b.enabled = true
              AND b.status IN ('CONFIRMED')
            """)
    Integer getIncomingBookingsCountByResourceId(@Param("resourceId") String resourceId, @Param("now") LocalDateTime now);

    Optional<BookingEntity> findByIdAndEnabledTrue(String id);

    List<BookingEntity> findAllByIdInAndEnabledTrue(List<String> bookingIds);

    @Query("""
                SELECT COALESCE(MAX(b.bookingNumber), 0)
                FROM BookingEntity b
                JOIN b.slotTimeEntity st
                JOIN st.offeringEntity o
                WHERE b.phoneNumber = :phoneNumber
                  AND o.userEntity.id = :userId
                  AND b.status IN ('CONFIRMED', 'CANCELLED')
            """)
    Integer findMaxBookingNumber(String phoneNumber, String userId);
}
