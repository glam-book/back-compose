package com.tlback.domain.notifier;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.tlback.domain.notifier.api.NotificationAttachment;


public class AsyncDelegatingNotificationAdapter<T> extends NotificationAdapter<T> {

    private final List<NotificationAdapter<T>> delegates;
    private final ExecutorService executorService;

    public AsyncDelegatingNotificationAdapter(List<NotificationAdapter<T>> delegates, ExecutorService executorService) {
        this.delegates = delegates;
        this.executorService = executorService;
    }

    @Override
    protected void sendMessage(T user, String message) {
        delegates.forEach(delegate ->
                executorService.submit(() ->
                        delegate.sendMessage(user, message)));
    }

    @Override
    protected void photoHandler(T user, NotificationAttachment att) {
        delegates.forEach(delegate -> {
            executorService.submit(() -> {
                delegate.photoHandler(user, att);
            });
        });
    }

    @Override
    protected void defaultHandler(T user, NotificationAttachment att) {
        delegates.forEach(delegate -> {
            executorService.submit(() -> {
                delegate.defaultHandler(user, att);
            });
        });
    }
}
