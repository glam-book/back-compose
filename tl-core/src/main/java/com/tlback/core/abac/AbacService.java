package com.tlback.core.abac;

import reactor.core.publisher.Mono;

public interface AbacService {
    Mono<AbacContext> canModifyRecord(Long userId, Long recordId);
    Mono<AbacContext> canAttachToService(Long userId, Long serviceId);
    Mono<AbacContext> canUseService(Long userId, Long serviceId);
}
