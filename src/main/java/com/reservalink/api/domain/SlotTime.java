package com.reservalink.api.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SlotTime {
    private String id;
    private Offering offering;
    private String resourceId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Double price;
    private Integer capacityAvailable;
    private Integer maxCapacity;
    private Boolean enabled;
}