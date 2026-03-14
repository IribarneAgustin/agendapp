package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, String> {

    @Query("SELECT s.id FROM SubscriptionEntity s " +
            "JOIN UserEntity u ON u.subscriptionEntity.id = s.id " +
            "JOIN OfferingEntity o ON o.userEntity.id = u.id " +
            "JOIN SlotTimeEntity st ON st.offeringEntity.id = o.id " +
            "JOIN BookingEntity b ON b.slotTimeEntity.id = st.id " +
            "WHERE b.id = :bookingId " +
            "AND s.expired = false")
    String findSubscriptionIdByBookingId(@Param("bookingId") String bookingId);
}
