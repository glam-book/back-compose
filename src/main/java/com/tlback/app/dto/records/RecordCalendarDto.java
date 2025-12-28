package com.tlback.app.dto.records;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecordCalendarDto(
        int day,
        OffsetDateTime ts,
        String text,
        String color,
        boolean isOwner,
        boolean canPending,
        boolean hasPendings) {
}
