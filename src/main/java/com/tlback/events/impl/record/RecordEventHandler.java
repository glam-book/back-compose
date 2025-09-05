package com.tlback.events.impl.record;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.tlback.core.service.UserService;
import com.tlback.events.impl.AbstractEventHandler;
import com.tlback.tg.balancer.TelegramClientBalanced;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RecordEventHandler extends AbstractEventHandler<RecordCreatedEvent> {
    private final TelegramClientBalanced tgClient;
    private final UserService userService;

    @Override
    public void handle(RecordCreatedEvent event) {
        log.info("Handling event: " + event.getEventType());
        var userId = event.getUserId();
        userService.findById(userId)
                .doOnSuccess(domainUser -> {
                    log.info("Trying to publish notification...");
                    domainUser.getTgUser().ifPresent(tgUser -> {
                        var tgId = tgUser.getId();
                        var sendMsg = new SendMessage(tgId.toString(),
                                String.format("""
                                        🎯 Запись успешно создана!
                                        ⏰ Время: %s : %s
                                        """, event.getStart(), event.getEnd()));
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
