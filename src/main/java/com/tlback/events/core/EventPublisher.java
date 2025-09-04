package com.tlback.events.core;

public interface EventPublisher {
    void publish(DomainEvent event);
}


