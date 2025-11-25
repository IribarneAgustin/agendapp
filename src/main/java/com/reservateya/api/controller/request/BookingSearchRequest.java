package com.reservateya.api.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BookingSearchRequest {
    private Integer pageNumber = 0;
    private Integer pageSize = 10;
    private String clientName;
    private LocalDateTime startDate;
    private String month;
    private String offeringId;
}
