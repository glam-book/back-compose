package com.tlback.domain.model.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public final class GenericContact implements ContactProvider {
    private final String genericIdentity;

    @Override
    public Supports support() {
        return Supports.RAW;
    }   
}
