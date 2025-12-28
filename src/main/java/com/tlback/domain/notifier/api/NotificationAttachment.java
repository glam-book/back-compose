package com.tlback.domain.notifier.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationAttachment {
    private byte[] data;
    private MediaType mediaType;
    private String fileName; // Required
    private String attachmentText;

    public boolean hasAttamentText() {
        return attachmentText != null;
    }
}
