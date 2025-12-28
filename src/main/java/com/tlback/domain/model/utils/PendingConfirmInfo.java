package com.tlback.domain.model.utils;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Builder;

@Builder
public record PendingConfirmInfo(
        @NonNull Long pendingId,
        @NonNull Set<String> serviceName,
        @NonNull OffsetDateTime from, 
        @Nullable OffsetDateTime to) implements Serializable {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy 'в' HH:mm")
            .withLocale(Locale.of("ru"));
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withLocale(Locale.of("ru"));

    public String toConfirmationMessage() {
        var services = (serviceName == null || serviceName.isEmpty())
                ? "услуга"
                : String.join(", ", serviceName);

        String timePart;
        if (to == null) {
            timePart = from.format(DATE_TIME_FORMAT);
        } else {
            timePart = String.format("%s — %s", from.format(DATE_TIME_FORMAT), to.format(TIME_FORMAT));
        }

        return String.format("Вы были записаны: %s.%n📅 %s%nПридёте?", services, timePart);
    }
}
