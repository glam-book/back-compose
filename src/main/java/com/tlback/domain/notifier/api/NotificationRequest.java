package com.tlback.domain.notifier.api;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class NotificationRequest {
    private String message;

    @Singular
    private List<NotificationAttachment> attachments;

    public boolean hasMessage() {
        return message != null;
    }

    public boolean hasAttachment() {
        return attachments != null && !attachments.isEmpty();
    }
}
