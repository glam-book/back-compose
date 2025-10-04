package com.tlback.events.impl.record;

import java.time.OffsetDateTime;

import com.tlback.events.core.DomainEvent;

import lombok.Getter;

@Getter
public class RecordUpdatedEvent extends DomainEvent {

    public static final String TYPE = "RECORD_UPDATED_EVENT";

    private final Long userId;
    private final Long recId;
    private final OffsetDateTime start;
    private final OffsetDateTime end;

    public RecordUpdatedEvent(Object source, 
        Long userId, 
        Long recId, 
        OffsetDateTime start, 
        OffsetDateTime end) {

        super(source);
        this.userId = userId;
        this.recId = recId;
        this.start = start;
        this.end = end;
    }

    @Override
    public String getEventType() {
        return TYPE;
    }
    
}
