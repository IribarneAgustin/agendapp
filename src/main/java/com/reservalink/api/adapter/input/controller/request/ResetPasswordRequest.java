package com.reservalink.api.adapter.input.controller.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(@NotBlank String token, @NotBlank String newPassword) {
}
