package com.reservalink.api.application.service.packageSession;

import com.reservalink.api.adapter.input.controller.request.OfferingRequest;
import com.reservalink.api.adapter.input.controller.response.OfferingResponse;

public interface PackageSessionService {
    void createPackageSessionTemplate(OfferingRequest request, OfferingResponse response);

    void enrichOfferingResponse(OfferingResponse response);
}
