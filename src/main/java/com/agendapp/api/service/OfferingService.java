package com.agendapp.api.service;

import com.agendapp.api.controller.request.OfferingRequest;
import com.agendapp.api.controller.response.OfferingResponse;

import java.util.List;
import java.util.UUID;

public interface OfferingService {
    OfferingResponse create(OfferingRequest offeringRequest);

    OfferingResponse update(OfferingRequest offeringRequest) throws Exception;

    List<OfferingResponse> findAllByUserId(UUID userId);

    void delete(UUID offeringId);
}
