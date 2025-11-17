package com.agendapp.api.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionRequest {
    @NotNull
    private String userId;

    @NotNull
    @Email
    private String userEmail;

    @NotNull
    private String cardToken;

}