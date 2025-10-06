package com.tlback.notifier.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.tlback.core.abac.exception.NotFoundException;
import com.tlback.core.model.DomainUserEntity;
import com.tlback.core.service.UserService;
import com.tlback.notifier.UserNotifier;
import com.tlback.notifier.model.MediaType;
import com.tlback.notifier.model.NotificationAttachment;
import com.tlback.notifier.model.NotificationRequest;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class NotificationAdapter implements UserNotifier {
    protected UserService userService;

    protected Map<MediaType, BiConsumer<DomainUserEntity, NotificationAttachment>> typeHandlers = 
        Map.of(MediaType.PHOTO, (id, att) -> this.photoHandler(id, att));

    @Override
    public void sendNotification(Long userId, NotificationRequest request) {
        userService.findById(userId).doOnSuccess(user -> {
            sendNotification(user, request);
        }).switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)));
    }

    @Override
    public void sendInBatch(List<Long> userIds, NotificationRequest request) {
        var users = userService.findByIds(userIds);
        users.doOnNext(user -> sendNotification(user, request));
    }

    @Override
    public void sendToAll(NotificationRequest request) {
        var users = userService.findAll();
        users.doOnNext(user -> sendNotification(user, request));
    }

    private void sendNotification(DomainUserEntity user, NotificationRequest request) {
        if (request.hasMessage())
            sendMessage(user, request.getMessage());

        if (request.hasAttachment())
            request.getAttachments().forEach(attachment -> 
                sendAttachment(user, attachment));
    }


    protected abstract void sendMessage(DomainUserEntity user, String message);

    protected abstract void photoHandler(DomainUserEntity user, NotificationAttachment att);

    protected abstract void defaultHandler(DomainUserEntity user, NotificationAttachment att);

    private BiConsumer<DomainUserEntity, NotificationAttachment> getHandler(MediaType mediaType) {
        return Optional.ofNullable(typeHandlers.get(mediaType))
            .orElse((id, att) -> this.defaultHandler(id, att));
    }

    private void sendAttachment(DomainUserEntity userId, NotificationAttachment attachment) {
        var handler = getHandler(attachment.getMediaType());
        handler.accept(userId, attachment);
    }
}
