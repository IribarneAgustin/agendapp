package com.reservalink.api.controller.request;

import jakarta.validation.constraints.Email;

public record RecoverPasswordRequest(@Email String email, String password) {
}
