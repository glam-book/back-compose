package com.tlback.web.dto;

import lombok.Builder;

@Builder
public record ServiceOwnerInfoDto(
        Long id,
        String serviceName,
        String serviceDescription,
        Integer recordLimit,
        Integer timeDuration
) {
}

