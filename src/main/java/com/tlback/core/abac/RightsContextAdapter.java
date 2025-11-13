package com.tlback.core.abac;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tlback.core.abac.PermissionMask.Rights;
import com.tlback.core.abac.exception.RightsException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RightsContextAdapter {
    private final Rights rights;
    private final boolean isOwner;

    public static RightsContextAdapter of(byte[] mask, boolean isOwner) {
        return new RightsContextAdapter(new Rights(mask), isOwner);
    }

    public <T> T onWriteAllowMap(Supplier<T> mapper, Function<RightsException, T> onDenied) {
        return rights.canWrite(isOwner) ? 
            mapper.get() : onDenied.apply(mapException());
    }

    public <T> Optional<T> onWriteAllowMap(Supplier<T> mapper) {
        return rights.canWrite(isOwner) ? 
            Optional.ofNullable(mapper.get()) : Optional.empty();
    }

    public <T> T onReadAllowMap(Supplier<T> mapper, Function<RightsException, T> onDenied) {
        return rights.canRead(isOwner) ?
            mapper.get() : onDenied.apply(mapException());
    }

    public <T> Optional<T> onReadAllowMap(Supplier<T> mapper) {
        return rights.canRead(isOwner) ?
            Optional.ofNullable(mapper.get()) : Optional.empty();
    }

    public RightsException mapException() {
        return new RightsException("You have no rights to access requested resource");
    }
    
}
