package com.reservalink.api.application.dto;

public record PaymentDetails(
        String id,
        String externalReference,
        String status
) {
    public boolean isApproved() {
        return "approved".equalsIgnoreCase(status);
    }
}