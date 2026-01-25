package com.reservalink.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Resource {
    private String id;
    private String userId;
    private String name;
    private String lastName;
    private Boolean isDefault;
    private Boolean enabled;
}