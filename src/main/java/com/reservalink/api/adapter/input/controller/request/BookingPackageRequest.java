package com.reservalink.api.adapter.input.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingPackageRequest {
    @NotNull
    private String offeringId;
    @NotNull
    private List<String> slotTimeIds;
    @NotNull
    private String customerEmail;
    @NotNull
    private String customerName;
    @NotNull
    private String customerPhone;
    @NotNull
    private Integer quantity;
}
