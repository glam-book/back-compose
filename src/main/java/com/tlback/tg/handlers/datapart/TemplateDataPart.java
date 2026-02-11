package com.tlback.tg.handlers.datapart;

public record TemplateDataPart(String template) implements DataPart {
    public static final String PREFIX = "/";

    public static TemplateDataPart of(String rawData) {
        return (TemplateDataPart) parseInternal(rawData);
    }

    @Override
    public String prefix() {
        return PREFIX;
    }

    @Override
    public String convert() {
        return prefix() + template;
    }

    @Override
    public DataPart parse(String data) {
        return parseInternal(data);
    }

    public static DataPart parseInternal(String data) {
        if (data == null || !data.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Invalid format for TemplateDataPart: " + data);
        }
        String templateValue = data.substring(PREFIX.length());
        return new TemplateDataPart(templateValue);
    }
}
