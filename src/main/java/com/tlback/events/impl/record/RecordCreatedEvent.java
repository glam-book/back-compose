package com.tlback.events.impl.record;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.tlback.events.core.DomainEvent;

import lombok.Builder;

@Builder
public record RecordCreatedEvent(Long userId, Long recId, 
    OffsetDateTime start, OffsetDateTime end, Instant timestamp) implements DomainEvent {
    public static final String TYPE = "RECORD_CREATED_EVENT";

    @Override
    public String getEventType() {
        return TYPE;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
    
}
