package com.tlback.notifier;

import java.util.List;

import com.tlback.notifier.model.NotificationRequest;

public interface UserNotifier {
    void sendNotification(Long userId, NotificationRequest request);
    void sendToAll(NotificationRequest request);
    void sendInBatch(List<Long> userIds, NotificationRequest request);
}
