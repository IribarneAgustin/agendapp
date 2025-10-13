package com.agendapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserDTO {
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String brandName;
    private Boolean enabled;
}
