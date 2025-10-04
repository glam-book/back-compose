package com.tlback.events.impl.record;

import org.springframework.stereotype.Component;

import com.tlback.core.service.RecordService;
import com.tlback.events.impl.AbstractEventHandler;
import com.tlback.notifier.UserNotifier;
import com.tlback.notifier.model.NotificationRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecordUpdateHandler extends AbstractEventHandler<RecordUpdatedEvent> {
    private final RecordService recordService;
    private final UserNotifier userNotifier;

    @Override
    public void handle(RecordUpdatedEvent event) {
        var recId = event.getRecId();
        var fluxPendings = recordService.getRecordPendings(recId);
        fluxPendings.collectList().doOnNext(it -> {
            var userIds = it.stream().map(pending -> pending.getClientId()).toList();
            var notificationRequest = NotificationRequest.builder()
                .message(String.format("Ваша запись %s перенесена start: %s, end: %s", 
                    event.getRecId(), event.getStart(), event.getEnd()))
                .build();
            userNotifier.sendInBatch(userIds, notificationRequest);
        });
    }

    @Override
    public Class<RecordUpdatedEvent> getEventType() {
        return RecordUpdatedEvent.class;
    }
    
}
