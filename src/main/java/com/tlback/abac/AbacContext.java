package com.tlback.abac;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.tlback.abac.exception.ForbiddenException;
import com.tlback.abac.exception.NotFoundException;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(of = {"decision"})
public class AbacContext {
    private final AbacDecision decision;
    private final String reason;

    private AbacContext(AbacDecision decision, String reason) {
        this.decision = decision;
        this.reason = reason;
    }

    public static AbacContext allow() {
        log.debug("ABAC allow");
        return new AbacContext(AbacDecision.ALLOW, null);
    }

    public static AbacContext deny(String reason) {
        log.debug("ABAC deny: {}", reason);
        return new AbacContext(AbacDecision.DENY, reason);
    }

    public static AbacContext notFound(String what) {
        return new AbacContext(AbacDecision.NOT_FOUND, what + " not found");
    }

    public boolean isAllowed() {
        return decision == AbacDecision.ALLOW;
    }

    public RuntimeException mapException() {
        return switch (decision) {
                case NOT_FOUND -> new NotFoundException(reason);
                case DENY -> new ForbiddenException(reason);
                default -> new IllegalStateException("Unexpected ABAC state");
        };
    }

    public AbacContext orThrow() {
        return orThrow(this::mapException);
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

    public <T> Optional<T> onAllowedMap(Supplier<T> mapper) {
        return isAllowed() ? Optional.ofNullable(mapper.get()) : Optional.empty();
    }

    public <T> T onAllowedMap(Supplier<T> mapper, Function<AbacDecision, T> onDenied) {
        return isAllowed() ? mapper.get() : onDenied.apply(decision);
    }

    public <T> T mapResult(Supplier<T> onSuccess, Function<RuntimeException, T> onError) {
        return isAllowed() ? onSuccess.get() : onError.apply(mapException());
    }

    public <T> Optional<T> onDeniedMap(Function<String, T> handler) {
        return decision == AbacDecision.DENY ? Optional.ofNullable(handler.apply(reason)) : Optional.empty();
    }

    public AbacDecision decision() {
        return decision;
    }

    public String reason() {
        return reason;
    }
}
