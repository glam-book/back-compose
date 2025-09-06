package com.tlback.events.impl;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

import com.tlback.events.core.DomainEvent;
import com.tlback.events.core.EventHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractEventHandler<T extends DomainEvent>
        implements EventHandler<T>, ApplicationListener<ApplicationEvent> {

    @Override
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(@NonNull ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof DomainEvent event && getEventType().isInstance(applicationEvent)) {
            try {
                log.info("Handling domain event: " + event.getClass());
                handle(getEventType().cast(applicationEvent));
            } catch (Exception e) {
                log.error("Error handling event: {}", event.getEventType(), e);
                handleError((T) event, e);
            }
        }
    }

    protected void handleError(T event, Exception e) {
        log.warn("Failed to process event {}: {}", event.getEventType(), e.getMessage());
    }
}
