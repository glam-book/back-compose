package com.tlback.scheduling.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobKeyDTO {
    private String identity;
    private String group;
    private String fullKey;
}
