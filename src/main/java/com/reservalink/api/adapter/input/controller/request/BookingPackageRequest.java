package com.reservalink.api.adapter.input.controller.request;

import lombok.Data;
import java.util.List;

@Data
public class BookingPackageRequest {
    private String offeringId;
    private List<String> slotTimeIds;
    private String customerEmail;
    private String customerName;
    private String customerPhone;
}
