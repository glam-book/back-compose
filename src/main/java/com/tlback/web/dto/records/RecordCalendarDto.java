package com.tlback.web.dto.records;

import java.time.OffsetDateTime;

import lombok.Builder;

@Builder
public record RecordCalendarDto(
        int day,
        OffsetDateTime ts,
        boolean isOwner,
        boolean canPending,
        boolean hasPendings) {
}
