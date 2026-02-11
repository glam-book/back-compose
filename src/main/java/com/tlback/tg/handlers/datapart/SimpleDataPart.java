package com.tlback.tg.handlers.datapart;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public record SimpleDataPart(List<String> parts) implements DataPart {

    public static final String PREFIX = "^";

    public static SimpleDataPart of(String rawData) {
        return (SimpleDataPart) parseInternal(rawData);
    }

    public static SimpleDataPart of(String... parts) {
        return new SimpleDataPart(List.of(parts));
    }

    public static SimpleDataPart of(Object... parts) {
        return new SimpleDataPart(Stream.of(parts).map(it -> it.toString()).toList());
    }

    @Override
    public String prefix() {
        return PREFIX;
    }

    @Override
    public String convert() {
        return prefix() + String.join("|", parts);
    }

    @Override
    public DataPart parse(String data) {
        return parseInternal(data);
    }

    public static DataPart parseInternal(String data) {
        if (data == null || !data.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Invalid format for SimpleDataPart: " + data);
        }
        var content = data.substring(PREFIX.length());
        List<String> parsedParts = content.isEmpty()
                ? List.of()
                : Arrays.stream(content.split("\\|", -1)).toList();
        return new SimpleDataPart(parsedParts);
    }

}
