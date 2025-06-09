package com.tlback.tools;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class ZoneOffsetTools {
    public static final String DEFAULT_OFFSET = "+08:00";

    private ZoneOffsetTools() {
    }

    public static ZoneOffset getCurrentOffset() {
        return ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
    }
}
