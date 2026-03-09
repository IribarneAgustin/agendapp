package com.reservalink.api.application.output;

import com.reservalink.api.domain.PackageSession;
import java.util.Optional;

public interface PackageSessionRepositoryPort {
    PackageSession save(PackageSession packageSession);

    Optional<PackageSession> findById(String id);

    Optional<PackageSession> findByOfferingId(String offeringId);
}
