package com.tlback.notifier.model;

import java.util.Set;

public enum MediaType {

    PHOTO, VIDEO, PLAIN_DOCUMENT, BINARY;

    private static final Set<String> photoExt = Set.of("jpg", "jpeg", "png", "bmp");
    private static final Set<String> videoExt = Set.of("mp4", "mov");

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
