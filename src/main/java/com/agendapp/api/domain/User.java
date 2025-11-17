package com.agendapp.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class User {
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String brandName;
    private Boolean enabled;
}
