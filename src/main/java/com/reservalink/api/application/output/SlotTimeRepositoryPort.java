package com.reservalink.api.application.output;

import com.reservalink.api.domain.SlotTime;

import java.util.List;
import java.util.Optional;

public interface SlotTimeRepositoryPort {
    Optional<SlotTime> findById(String id);

    void save(SlotTime slotTime);

    List<SlotTime> findByOfferingEntityIdAndEnabledTrue(String string);

    void saveAll(List<SlotTime> slots);
}