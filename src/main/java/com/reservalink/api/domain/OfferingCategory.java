package com.reservalink.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferingCategory {
    private String id;
    private String userId;
    private String name;
    private Boolean enabled;
    private Boolean isDefault;
}