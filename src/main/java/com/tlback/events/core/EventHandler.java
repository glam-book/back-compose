package com.tlback.events.core;

public interface EventHandler<T extends DomainEvent> {
    void handle(T event);
    Class<T> getEventType();
}
