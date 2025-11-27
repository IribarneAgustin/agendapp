package com.reservalink.api.controller.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OfferingRequest {

    private UUID id;

    @NotNull(message = "User is required")
    private UUID userId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Min(value = 0, message = "Advance payment must be at least 0%")
    @Max(value = 100, message = "Advance payment cannot exceed 100%")
    private Integer advancePaymentPercentage;

}
