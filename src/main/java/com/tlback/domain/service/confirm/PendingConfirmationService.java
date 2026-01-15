package com.tlback.domain.service.confirm;

import reactor.core.publisher.Mono;

public interface PendingConfirmationService {

    Mono<Void> onPendingCreated(Long pendingId);

    Mono<Void> onPendingCancelled(Long pendingId);

    Mono<Void> onPendingConfirmed(Long pendingId);
}
