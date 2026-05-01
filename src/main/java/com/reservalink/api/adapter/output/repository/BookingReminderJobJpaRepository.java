package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BookingReminderJobEntity;
import com.reservalink.api.domain.enums.BookingReminderJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingReminderJobJpaRepository extends JpaRepository<BookingReminderJobEntity, String> {

    List<BookingReminderJobEntity> findTop100ByStatusInAndTriggerDatetimeLessThanEqualOrderByTriggerDatetimeAsc(List<BookingReminderJobStatus> status, LocalDateTime triggerDatetime);

    List<BookingReminderJobEntity> findAllByEnabledTrueAndBooking_IdAndStatusIn(String bookingId, List<BookingReminderJobStatus> statusList);
}