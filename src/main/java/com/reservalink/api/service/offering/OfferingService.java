package com.reservalink.api.service.offering;

import com.reservalink.api.controller.request.OfferingRequest;
import com.reservalink.api.controller.response.OfferingResponse;

import java.util.List;
import java.util.UUID;

public interface OfferingService {
    OfferingResponse create(OfferingRequest offeringRequest);

    OfferingResponse update(OfferingRequest offeringRequest) throws Exception;

    List<OfferingResponse> findAllByUserId(UUID userId);

    void delete(UUID offeringId);
}
