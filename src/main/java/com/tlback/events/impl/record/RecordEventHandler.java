package com.tlback.events.impl.record;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.tlback.core.service.UserService;
import com.tlback.events.impl.AbstractEventHandler;
import com.tlback.tg.balancer.TelegramClientBalanced;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecordEventHandler extends AbstractEventHandler<RecordCreatedEvent> {
    private final TelegramClientBalanced tgClient;
    private final UserService userService;

    @Override
    public void handle(RecordCreatedEvent event) {
        var userId = event.userId();
        userService.findByTgId(userId)
                .doOnSuccess(domainUser -> {
                    domainUser.getTgUser().ifPresent(tgUser -> {
                        var tgId = tgUser.getId();
                        var sendMsg = new SendMessage(tgId.toString(),
                                String.format("""
                                        🎯 Запись успешно создана!
                                        ⏰ Время: %s : %s
                                        """, event.start(), event.end()));
                        tgClient.executeGeneric(sendMsg);
                    });
                })
                .subscribe();
    }

    @Override
    public Class<RecordCreatedEvent> getEventType() {
        return RecordCreatedEvent.class;
    }

}
