package com.tlback.notifier.model;

import java.io.InputStream;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationAttachment {
    private InputStream inputStream;
    private MediaType mediaType;
    private String fileName; // Required
    private String attachmentText;

    public boolean hasAttamentText() {
        return attachmentText != null;
    }
}
