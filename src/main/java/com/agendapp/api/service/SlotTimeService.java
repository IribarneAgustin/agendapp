package com.agendapp.api.service;

import com.agendapp.api.controller.request.SlotTimeRequest;
import com.agendapp.api.controller.response.SlotTimeResponse;
import com.agendapp.api.entity.SlotTime;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SlotTimeService {
    List<SlotTimeResponse> createList(List<SlotTimeRequest> slotTimeRequestList);

    Page<SlotTimeResponse> findNextSlotsPageByOfferingId(UUID offeringId, Pageable pageable);

    SlotTimeResponse update(UUID slotTimeId, SlotTimeRequest slotTimeRequest);

    void delete(UUID slotTimeId);
}
