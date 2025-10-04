package com.agendapp.api.controller.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SlotTimeResponse {
    private String id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Double price;
    private Integer capacityAvailable;
    private Boolean active;
}
