package com.tlback.events.impl.pending;

import com.tlback.events.core.DomainEvent;

import lombok.Getter;

@Getter
public class RecordPendingCreatedEvent extends DomainEvent {
    private final Long recordOwnerId;
    private final Long recordId;

    public RecordPendingCreatedEvent(Object source, Long recordOwnerId, Long recordId) {
        super(source);
        this.recordOwnerId = recordOwnerId;
        this.recordId = recordId;
    }

    @Override
    public String getEventType() {
        return "RECORD_PENDING_CREATED";
    }
    
}
