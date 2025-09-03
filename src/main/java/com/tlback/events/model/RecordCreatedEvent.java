package com.tlback.events.model;

import java.time.LocalDateTime;

public record RecordCreatedEvent(Long userId, Long recId, LocalDateTime start, LocalDateTime end) {
    
}
