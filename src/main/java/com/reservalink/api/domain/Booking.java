package com.reservalink.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Booking {
    private String id;
    private String slotTimeId;
    private String email;
    private String phoneNumber;
    private String name;
    private Integer quantity;
    private BookingStatus status;
}