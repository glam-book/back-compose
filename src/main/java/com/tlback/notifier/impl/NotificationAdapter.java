package com.tlback.notifier.impl;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.tlback.core.service.UserService;
import com.tlback.notifier.UserNotifier;
import com.tlback.notifier.model.MediaType;
import com.tlback.notifier.model.NotificationAttachment;
import com.tlback.notifier.model.NotificationRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class NotificationAdapter<T> implements UserNotifier<T> {
    protected UserService userService;

    protected Map<MediaType, BiConsumer<T, NotificationAttachment>> typeHandlers = Map.of(
        MediaType.PHOTO,
            (id, att) -> this.photoHandler(id, att));

    @Override
    public void sendToAll(NotificationRequest request) {
    }

    @Override
    public void sendNotification(T user, NotificationRequest request) {
        if (request.hasMessage())
            sendMessage(user, request.getMessage());

        if (request.hasAttachment())
            request.getAttachments()
                    .forEach(attachment -> sendAttachment(user, attachment));
    }

    protected abstract void sendMessage(T user, String message);

    protected abstract void photoHandler(T user, NotificationAttachment att);

    protected abstract void defaultHandler(T user, NotificationAttachment att);

    private BiConsumer<T, NotificationAttachment> getHandler(MediaType mediaType) {
        return Optional.ofNullable(typeHandlers.get(mediaType))
                .orElse((id, att) -> this.defaultHandler(id, att));
    }

    private void sendAttachment(T user, NotificationAttachment attachment) {
        var handler = getHandler(attachment.getMediaType());
        handler.accept(user, attachment);
    }

    @Override
    public void sendInBatch(Iterable<T> domainUsers, NotificationRequest request) {
        domainUsers.forEach(user -> sendNotification(user, request));
    }

}