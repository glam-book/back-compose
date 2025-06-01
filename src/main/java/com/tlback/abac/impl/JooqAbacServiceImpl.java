package com.tlback.abac.impl;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.stereotype.Service;

import com.tlback.abac.AbacContext;
import com.tlback.abac.AbacService;
import com.tlback.jooq.gen.tables.Record;
import com.tlback.jooq.gen.tables.ServiceInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JooqAbacServiceImpl implements AbacService {
    private static final Record RECORD_TABLE = Record.RECORD;
    private static final ServiceInfo SERVICE_INFO_TABLE = ServiceInfo.SERVICE_INFO;

    private final DSLContext dsl;

    @Override
    public AbacContext canModifyRecord(Long userId, Long recordId) {
        Record1<Long> record = dsl
            .select(RECORD_TABLE.RECORD_OWNER_ID)
            .from(RECORD_TABLE)
            .where(RECORD_TABLE.ID.eq(recordId))
            .fetchOne();

        if (record == null) {
            return AbacContext.notFound("Record");
        }

        Long ownerId = record.value1();
        return ownerId.equals(userId)
            ? AbacContext.allow()
            : AbacContext.deny("You do not own this record");
    }

    @Override
    public AbacContext canAttachToService(Long userId, Long serviceId) {
        var service = dsl
            .select(SERVICE_INFO_TABLE.SERVICE_OWNER_ID, SERVICE_INFO_TABLE.EDITABLE)
            .from(SERVICE_INFO_TABLE)
            .where(SERVICE_INFO_TABLE.ID.eq(serviceId))
            .fetchOne();

        if (service == null) {
            return AbacContext.notFound("Service");
        }

        Long ownerId = service.get(SERVICE_INFO_TABLE.SERVICE_OWNER_ID);
        Boolean editable = service.get(SERVICE_INFO_TABLE.EDITABLE);

        if (ownerId.equals(userId)) {
            return AbacContext.allow();
        }

        if (Boolean.TRUE.equals(editable)) {
            return AbacContext.allow();
        }

        return AbacContext.deny("You cannot attach records to this service");
    }
}
