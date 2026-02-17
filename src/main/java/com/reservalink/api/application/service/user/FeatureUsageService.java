package com.reservalink.api.application.service.user;


import com.reservalink.api.adapter.input.controller.request.FeatureUsageRequest;
import com.reservalink.api.application.dto.FeatureUsageDetail;
import com.reservalink.api.application.dto.FeatureUsageResponse;

import java.util.List;

public interface FeatureUsageService {

    FeatureUsageResponse create(FeatureUsageRequest subscriptionFeature, String userId);

    void delete(String featureUsageId, String userId);

    List<FeatureUsageDetail> findAllAvailableByUserId(String userId);

}