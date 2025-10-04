package com.agendapp.api.repository;

import com.agendapp.api.entity.SlotTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface SlotTimeRepository extends JpaRepository<SlotTime, String> {
    Page<SlotTime> findAllByOfferingIdAndActiveTrueAndEndDateTimeGreaterThanEqualOrderByStartDateTimeAsc(
            String offeringId,
            LocalDateTime now,
            Pageable pageable
    );

    List<SlotTime> findByOfferingIdAndActiveTrue(String string);
}
