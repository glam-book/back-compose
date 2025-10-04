package com.tlback.notifier.model;

public enum MediaType {
    PHOTO, VIDEO, PLAIN_DOCUMENT, BINARY;

    public static MediaType detectType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
            return MediaType.PHOTO;
        } else if (lower.endsWith(".mp4") || lower.endsWith(".mov")) {
            return MediaType.VIDEO;
        } else {
            throw new IllegalArgumentException("Unknown media type for file: " + fileName);
        }
    }
}
