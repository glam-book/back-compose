package com.tlback.tg.handlers.datapart;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public  record KeyValueDataPart(Map<String, String> part) implements DataPart {
        public static final String PREFIX = "?";

        public static KeyValueDataPart of(String rawData) {
            return (KeyValueDataPart) parseInternal(rawData);
        }

        @Override
        public String prefix() {
            return PREFIX;
        }

        @Override
        public String convert() {
            return prefix() + part.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
        }

        @Override
        public DataPart parse(String data) {
            return parseInternal(data);
        }

        public static DataPart parseInternal(String rawData) {
            if (rawData == null || !rawData.startsWith(PREFIX)) {
                throw new IllegalArgumentException("Invalid format for KeyValueDataPart: " + rawData);
            }
            String content = rawData.substring(PREFIX.length());
            Map<String, String> map = new LinkedHashMap<>();
            if (!content.isEmpty()) {
                for (String pair : content.split("&")) {
                    String[] kv = pair.split("=", -1);
                    if (kv.length != 2) {
                        throw new IllegalArgumentException("Malformed key=value pair: " + pair);
                    }
                    map.put(kv[0], kv[1]);
                }
            }
            return new KeyValueDataPart(map);
        }

    }
