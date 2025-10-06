package com.tlback.notifier.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tlback.core.model.DomainUserEntity;
import com.tlback.notifier.model.NotificationAttachment;

public class AsyncDelegatingNotificationAdapter extends NotificationAdapter {

    private final List<NotificationAdapter> delegates;
    private final ExecutorService executorService;

    public AsyncDelegatingNotificationAdapter(List<NotificationAdapter> delegates) {
        this.delegates = delegates;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    protected void sendMessage(DomainUserEntity user, String message) {
        delegates.forEach(delegate -> {
            executorService.submit(() -> {
                delegate.sendMessage(user, message);
            });
        });
    }

    @Override
    protected void photoHandler(DomainUserEntity user, NotificationAttachment att) {
        delegates.forEach(delegate -> {
            executorService.submit(() -> {
                delegate.photoHandler(user, att);
            });
        });
    }

    @Override
    protected void defaultHandler(DomainUserEntity user, NotificationAttachment att) {
        delegates.forEach(delegate -> {
            executorService.submit(() -> {
                delegate.defaultHandler(user, att);
            });
        });
    }
    
}
