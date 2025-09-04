package com.tlback.events.core;

import java.time.Instant;

public interface DomainEvent {
    String getEventType();
    Instant getTimestamp();
}