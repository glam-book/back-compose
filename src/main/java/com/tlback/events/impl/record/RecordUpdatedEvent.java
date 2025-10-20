package com.tlback.events.impl.record;

import java.time.LocalDateTime;

import com.tlback.events.core.DomainEvent;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RecordUpdatedEvent extends DomainEvent {

    public static final String TYPE = "RECORD_UPDATED_EVENT";

    private final Long userId;
    private final Long recId;
    private final LocalDateTime start;
    private final LocalDateTime end;

    @Builder
    public RecordUpdatedEvent(Object source, 
        Long userId, 
        Long recId, 
        LocalDateTime start, 
        LocalDateTime end) {

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
