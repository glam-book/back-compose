package com.tlback.abac;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tlback.abac.exception.ForbiddenException;
import com.tlback.abac.exception.NotFoundException;

public class AbacContext {
    private final AbacDecision decision;
    private final String reason;

    private AbacContext(AbacDecision decision, String reason) {
        this.decision = decision;
        this.reason = reason;
    }

    public static AbacContext allow() {
        return new AbacContext(AbacDecision.ALLOW, null);
    }

    public static AbacContext deny(String reason) {
        return new AbacContext(AbacDecision.DENY, reason);
    }

    public static AbacContext notFound(String what) {
        return new AbacContext(AbacDecision.NOT_FOUND, what + " not found");
    }

    public boolean isAllowed() {
        return decision == AbacDecision.ALLOW;
    }

    public AbacContext orThrow() {
        return orThrow(() ->
            switch (decision) {
                case NOT_FOUND -> new NotFoundException(reason);
                case DENY -> new ForbiddenException(reason);
                default -> new IllegalStateException("Unexpected ABAC state");
        });
    }

    public AbacContext orThrow(Supplier<? extends RuntimeException> exSupplier) {
        if (!isAllowed()) throw exSupplier.get();
        return this;
    }

    public AbacContext onAllowed(Runnable action) {
        if (isAllowed()) action.run();
        return this;
    }

    public AbacContext onDenied(Consumer<String> onReason) {
        if (decision == AbacDecision.DENY) onReason.accept(reason);
        return this;
    }

    public <T> Optional<T> mapIfAllowed(Supplier<T> mapper) {
        return isAllowed() ? Optional.ofNullable(mapper.get()) : Optional.empty();
    }

    public <T> Optional<T> mapIfDenied(Function<String, T> handler) {
        return decision == AbacDecision.DENY ? Optional.ofNullable(handler.apply(reason)) : Optional.empty();
    }

    public AbacDecision decision() {
        return decision;
    }

    public String reason() {
        return reason;
    }
}
