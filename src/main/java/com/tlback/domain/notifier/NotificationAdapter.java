package com.tlback.domain.notifier;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.tlback.domain.notifier.api.MediaType;
import com.tlback.domain.notifier.api.NotificationAttachment;
import com.tlback.domain.notifier.api.NotificationRequest;
import com.tlback.domain.notifier.api.UserNotifier;
import com.tlback.domain.service.UserService;

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