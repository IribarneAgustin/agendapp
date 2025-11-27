package com.reservalink.api.service.booking;

import com.reservalink.api.controller.request.SlotTimeRequest;
import com.reservalink.api.controller.response.SlotTimeResponse;
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
