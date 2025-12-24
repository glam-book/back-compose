package com.tlback.scheduling;

public final class QrtzUtils {

    private QrtzUtils() {
    }

    public static String getJobKeyByIdentity(String id, String type) {
        return id + ":" + type;
    }
}
