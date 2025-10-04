package com.tlback.events.impl.record;

import com.tlback.core.service.UserService;
import com.tlback.events.impl.AbstractEventHandler;
import com.tlback.tg.balancer.TelegramClientGroupping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RecordEventHandler extends AbstractEventHandler<RecordCreatedEvent> {
    private final TelegramClientGroupping tgClient;
    private final UserService userService;

    @Override
    public void handle(RecordCreatedEvent event) {
    }

    @Override
    public Class<RecordCreatedEvent> getEventType() {
        return RecordCreatedEvent.class;
    }

}
