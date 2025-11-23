package com.agendapp.api.controller.response;

import com.agendapp.api.repository.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BookingGridResponse {
    private String id;
    private String clientEmail;
    private String clientName;
    private String clientPhone;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BookingStatus status; //CONFIRMED, CANCELLED
    private Double paid;
    private String serviceName;
    private Integer quantity;
}
