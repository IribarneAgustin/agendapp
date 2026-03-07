package com.reservalink.api.application.service.packageSession;

import com.reservalink.api.adapter.input.controller.request.OfferingRequest;
import com.reservalink.api.adapter.input.controller.response.OfferingResponse;
import com.reservalink.api.adapter.output.repository.entity.OfferingEntity;
import com.reservalink.api.adapter.output.repository.entity.PackageSessionEntity;
import com.reservalink.api.application.output.PackageSessionRepositoryPort;
import com.reservalink.api.domain.PackageSessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageSessionServiceImpl implements PackageSessionService {

    private final PackageSessionRepositoryPort packageSessionRepositoryPort;

    @Override
    @Transactional
    public void createPackageSessionTemplate(OfferingRequest request, OfferingResponse response) {
        if (request.getSessionLimit() != null && request.getSessionLimit() > 0 && request.getPackagePrice() != null) {
            log.info("Creating PackageSession template for offeringId: {}", response.getId());
            OfferingEntity offering = new OfferingEntity();
            offering.setId(response.getId());

            PackageSessionEntity entity = PackageSessionEntity.builder()
                    .offeringEntity(offering)
                    .sessionLimit(request.getSessionLimit())
                    .price(request.getPackagePrice())
                    .status(PackageSessionStatus.ACTIVE)
                    .enabled(true)
                    .build();

            packageSessionRepositoryPort.save(entity);

            response.setSessionLimit(request.getSessionLimit());
            response.setPackagePrice(request.getPackagePrice());
        }
    }

    @Override
    public void enrichOfferingResponse(OfferingResponse response) {
        packageSessionRepositoryPort.findByOfferingId(response.getId()).ifPresent(ps -> {
            response.setSessionLimit(ps.getSessionLimit());
            response.setPackagePrice(ps.getPrice());
        });
    }
}
