package com.tlback.common.daofilter;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record RecordFilter(
    Long serviceInfoId,
    Long recordOwnerId,
    Long clientId,
    LocalDateTime dateFrom,
    LocalDateTime dateTo,
    Boolean isPublic) {
}
