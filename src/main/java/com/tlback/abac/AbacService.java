package com.tlback.abac;

public interface AbacService {
    AbacContext canModifyRecord(Long userId, Long recordId);
    AbacContext canAttachToService(Long userId, Long serviceId);
}
