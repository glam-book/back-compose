package com.tlback.events.impl.pending;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.tlback.core.service.UserService;
import com.tlback.events.impl.AbstractEventHandler;
import com.tlback.tg.balancer.TelegramClientBalanced;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PendingCreatedHandler extends AbstractEventHandler<RecordPendingCreatedEvent> {
    private final TelegramClientBalanced tgClient;
    private final UserService userService;

    @Override
    public void handle(RecordPendingCreatedEvent event) {
        userService.findById(event.getRecordOwnerId())
                .doOnSuccess(domainUser -> {
                    domainUser.getTgUser().ifPresent(tgUser -> {
                        var tgId = tgUser.getId();
                        var sendMsg = new SendMessage(tgId.toString(),
                                String.format("""
                                        🎯 Новая заявка на запись 
                                        ⏰ Record handle id: %s : %s
                                        """, event.getRecordId()));
                        tgClient.executeGeneric(sendMsg);
                    });
                })
                .subscribe();
    }

    @Override
    public Class<RecordPendingCreatedEvent> getEventType() {
        return RecordPendingCreatedEvent.class;
    }
    
}