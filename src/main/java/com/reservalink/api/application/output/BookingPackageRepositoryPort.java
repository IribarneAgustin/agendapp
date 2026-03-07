package com.reservalink.api.application.output;

import com.reservalink.api.adapter.output.repository.entity.BookingPackageEntity;

import java.util.Optional;

public interface BookingPackageRepositoryPort {
    BookingPackageEntity save(BookingPackageEntity entity);

    Optional<BookingPackageEntity> findById(String id);
}
