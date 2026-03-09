package com.reservalink.api.application.service.payment.strategy;

import com.reservalink.api.application.service.booking.BookingPackageService;
import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.domain.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PackagePaymentStrategy implements PaymentProcessingStrategy {

    private final BookingPackageService bookingPackageService;

    @Override
    public boolean getType(PaymentType paymentType) {
        return PaymentType.PACKAGE.equals(paymentType);
    }

    @Override
    public void process(PaymentDetails paymentDetails) {
        if (paymentDetails.isApproved()) {
            String packageId = paymentDetails.externalReference().replace("PACKAGE-", "");
            bookingPackageService.confirmPackageBookings(packageId);
        }
    }
}