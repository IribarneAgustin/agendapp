package com.reservalink.api.adapter.input.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ResourceRequest(@NotNull String name, String lastName, boolean isDefault, @Email String email) {}