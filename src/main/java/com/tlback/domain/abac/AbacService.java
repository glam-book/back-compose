package com.tlback.domain.abac;

import com.tlback.domain.abac.PermissionMask.Rights;

import reactor.core.publisher.Mono;

public interface AbacService {
    Mono<AbacContext> canModifyRecord(Long userId, Long recordId);
    Mono<AbacContext> canAttachToService(Long userId, Long serviceId);
    Mono<AbacContext> canUseService(Long userId, Long serviceId);
    Mono<AbacContext> canUseRecord(Long recordId, Long owner);

    Mono<RightsContextAdapter> fetchRecordRights(Long recordId, Long requesterId);

    Rights parse(byte[] mask);
}
