package com.tlback.events.impl;

import org.springframework.context.ApplicationEventPublisher;

import com.tlback.events.core.DomainEvent;
import com.tlback.events.core.EventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        log.info("publishing event: " + event.getEventType());
        applicationEventPublisher.publishEvent(event);
    }
}

