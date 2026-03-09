package com.reservalink.api.application.service.offering;

import com.reservalink.api.adapter.input.controller.request.OfferingRequest;
import com.reservalink.api.adapter.input.controller.response.OfferingResponse;
import com.reservalink.api.application.output.PackageSessionRepositoryPort;
import com.reservalink.api.domain.PackageSession;
import com.reservalink.api.domain.PackageSessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageSessionServiceImpl implements PackageSessionService {

    private final PackageSessionRepositoryPort packageSessionRepositoryPort;

    @Override
    @Transactional
    public PackageSession processPackageSession(OfferingRequest request) {
        String offeringId = request.getId().toString();
        if (request.getSessionLimit() != null && request.getSessionLimit() > 0 && request.getPackagePrice() != null) {
            log.info("Processing PackageSession template (Create/Update) for offeringId: {}", offeringId);

            PackageSession packageSession = packageSessionRepositoryPort.findByOfferingId(offeringId)
                    .map(existing -> {
                        log.info("Updating existing PackageSession for offeringId: {}", offeringId);
                        existing.setSessionLimit(request.getSessionLimit());
                        existing.setPrice(BigDecimal.valueOf(request.getPackagePrice()));
                        existing.setEnabled(true);
                        existing.setStatus(PackageSessionStatus.ACTIVE);
                        return existing;
                    })
                    .orElseGet(() -> {
                        log.info("Creating new PackageSession for offeringId: {}", offeringId);
                        return PackageSession.builder()
                                .offeringId(offeringId)
                                .sessionLimit(request.getSessionLimit())
                                .price(BigDecimal.valueOf(request.getPackagePrice()))
                                .status(PackageSessionStatus.ACTIVE)
                                .enabled(true)
                                .build();
                    });

            return packageSessionRepositoryPort.save(packageSession);
        }

        log.info("No pack data provided for offeringId: {}. Checking for existing pack to disable.", offeringId);
        return packageSessionRepositoryPort.findByOfferingId(offeringId)
                .map(existing -> {
                    log.info("Disabling existing PackageSession for offeringId: {}", offeringId);
                    existing.setEnabled(false);
                    existing.setStatus(PackageSessionStatus.DISABLED);
                    return packageSessionRepositoryPort.save(existing);
                })
                .orElse(null);
    }

    @Override
    public void enrichOfferingResponse(OfferingResponse response) {
        packageSessionRepositoryPort.findByOfferingId(response.getId().toString()).ifPresent(ps -> {
            response.setSessionLimit(ps.getSessionLimit());
            response.setPackagePrice(ps.getPrice().doubleValue());
        });
    }
}