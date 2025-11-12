package com.tlback.notifier.model;

public enum MediaType {

    PHOTO, VIDEO, PLAIN_DOCUMENT, BINARY;

    public static MediaType detectType(String fileName) {
        String lower = fileName.toLowerCase();
        String group = lower.substring(0, lower.indexOf('/'));

        if (group.equals("image"))
            return MediaType.PHOTO;
        else if (group.equals("video"))
            return MediaType.VIDEO;
        else
            return BINARY;
    }
}
