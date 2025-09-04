package com.tlback.events.impl;

import org.springframework.context.ApplicationEventPublisher;

import com.tlback.events.core.DomainEvent;
import com.tlback.events.core.EventPublisher;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

