package com.tlback.tools;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class ZoneOffsetTools {
    private ZoneOffsetTools() {
    }

    public static ZoneOffset getCurrentOffset() {
        return ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
    }
}
