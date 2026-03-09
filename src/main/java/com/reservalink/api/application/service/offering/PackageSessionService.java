package com.reservalink.api.application.service.offering;

import com.reservalink.api.adapter.input.controller.request.OfferingRequest;
import com.reservalink.api.adapter.input.controller.response.OfferingResponse;
import com.reservalink.api.domain.PackageSession;

public interface PackageSessionService {
    PackageSession processPackageSession(OfferingRequest request);

    void enrichOfferingResponse(OfferingResponse response);
}
