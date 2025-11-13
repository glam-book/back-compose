package com.tlback.core.model.contact;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class TgContact implements ContactProvider {
    private final @Nullable String firstName;
    private final @Nullable String lastName;
    private final @Nullable String tgUserName;
}
