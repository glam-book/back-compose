package com.tlback.core.model.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public final class PhoneContact implements ContactProvider {
    private final PhoneNumber phoneNumber;

    @Override
    public Supports support() {
        return Supports.PHONE;
    }
}
