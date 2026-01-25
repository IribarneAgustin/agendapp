package com.reservalink.api.service.user;

import com.reservalink.api.domain.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ResourceService {

    List<Resource> findAllByUserId(String userId);

    List<Resource> findAllByUserIdAndOfferingId(String userId, String offeringId);

    Resource create(Resource resource);

    Resource update(Resource resource);

    @Transactional(rollbackFor = Exception.class)
    void delete(String resourceId);
}