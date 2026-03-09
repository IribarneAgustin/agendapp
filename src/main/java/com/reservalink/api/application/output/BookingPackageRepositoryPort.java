package com.reservalink.api.application.output;

import com.reservalink.api.domain.BookingPackage;

import java.util.Optional;

public interface BookingPackageRepositoryPort {
    BookingPackage save(BookingPackage domain);

    Optional<BookingPackage> findById(String id);
}