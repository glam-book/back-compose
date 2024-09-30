package com.tlback.web.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record RecordDto(
        Long id,
        Boolean isPublic,
        OffsetDateTime timeFrom,
        OffsetDateTime timeTo,
        ServiceOwnerInfoDto serviceInfo) {
}
