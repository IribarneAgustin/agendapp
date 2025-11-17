package com.agendapp.api.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BookingRequest {

    @NotNull
    private UUID slotTimeId;

    @NotNull
    private String email;

    @NotNull
    private String phoneNumber;

    @NotNull
    private String name;

    @NotNull
    private Integer quantity;
}
