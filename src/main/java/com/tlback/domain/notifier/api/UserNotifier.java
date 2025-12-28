package com.tlback.domain.notifier.api;

public interface UserNotifier<T> {
    void sendToAll(NotificationRequest request);
    void sendNotification(T domainUser, NotificationRequest request);
    void sendInBatch(Iterable<T> domainUsers, NotificationRequest request);
}
