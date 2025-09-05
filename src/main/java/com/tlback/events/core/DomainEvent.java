package com.tlback.events.core;

import org.springframework.context.ApplicationEvent;

public abstract class DomainEvent extends ApplicationEvent {

    public DomainEvent(Object source) {
        super(source);
    }

    public abstract String getEventType();
}