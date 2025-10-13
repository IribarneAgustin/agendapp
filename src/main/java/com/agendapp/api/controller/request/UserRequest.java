package com.agendapp.api.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserRequest {

    @NotNull
    @Length(min = 1, max = 30)
    private String name;
    @NotNull
    @Length(min = 1, max = 30)
    private String lastName;
    @Email
    @NotNull
    private String email;
    @NotNull
    private String phone;
    @NotNull
    private String brandName;
}