package com.tlback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tlback.abac.AbacContext;
import com.tlback.abac.AbacDecision;
import com.tlback.abac.impl.JooqAbacServiceImpl;
import com.tlback.jooq.gen.tables.Record;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JooqAbacServiceTest {
    private static final Record RECORD = Record.RECORD;

    private DSLContext dsl;
    private JooqAbacServiceImpl abacService;

    @BeforeEach
    void setUp() {
        dsl = mock(DSLContext.class);
        abacService = new JooqAbacServiceImpl(dsl);
    }

    @Test
    void testCanModifyRecord_asOwner() {
        @SuppressWarnings("unchecked")
        Record1<Long> mockedRecord = mock(Record1.class);
        when(mockedRecord.value1()).thenReturn(42L);

        @SuppressWarnings("unchecked")
        org.jooq.SelectSelectStep<Record1<Long>> selectStep = mock(org.jooq.SelectSelectStep.class);
        @SuppressWarnings("unchecked")
        org.jooq.SelectJoinStep<Record1<Long>> fromStep = mock(org.jooq.SelectJoinStep.class);
        @SuppressWarnings("unchecked")
        org.jooq.SelectConditionStep<Record1<Long>> whereStep = mock(org.jooq.SelectConditionStep.class);
        when(dsl.select(RECORD.RECORD_OWNER_ID)).thenReturn(selectStep);
        when(selectStep.from(RECORD)).thenReturn(fromStep);
        when(fromStep.where(RECORD.ID.eq(1L))).thenReturn(whereStep);
        when(whereStep.fetchOne()).thenReturn(mockedRecord);

        Mono<AbacContext> result = abacService.canModifyRecord(42L, 1L);

        StepVerifier.create(result)
                .assertNext(ctx -> assertTrue(ctx.isAllowed()))
                .verifyComplete();
    }

    @Test
    void testCanModifyRecord_notOwner() {
        @SuppressWarnings("unchecked")
        Record1<Long> mockedRecord = mock(Record1.class);
        when(mockedRecord.value1()).thenReturn(42L);

        @SuppressWarnings("unchecked")
        org.jooq.SelectSelectStep<Record1<Long>> selectStep = mock(org.jooq.SelectSelectStep.class);
        @SuppressWarnings("unchecked")
        org.jooq.SelectJoinStep<Record1<Long>> fromStep = mock(org.jooq.SelectJoinStep.class);
        @SuppressWarnings("unchecked")
        org.jooq.SelectConditionStep<Record1<Long>> whereStep = mock(org.jooq.SelectConditionStep.class);
        when(dsl.select(RECORD.RECORD_OWNER_ID)).thenReturn(selectStep);
        when(selectStep.from(RECORD)).thenReturn(fromStep);
        when(fromStep.where(RECORD.ID.eq(2L))).thenReturn(whereStep);
        when(whereStep.fetchOne()).thenReturn(mockedRecord);

        Mono<AbacContext> result = abacService.canModifyRecord(99L, 2L);

        StepVerifier.create(result)
                .assertNext(ctx -> {
                    assertFalse(ctx.isAllowed());
                    assertEquals(AbacDecision.DENY, ctx.decision());
                })
                .verifyComplete();
    }

    @Test
    void testCanModifyRecord_notFound() {
        @SuppressWarnings("unchecked")
        org.jooq.SelectSelectStep<Record1<Long>> selectStep = mock(org.jooq.SelectSelectStep.class);
        @SuppressWarnings("unchecked")
        org.jooq.SelectJoinStep<Record1<Long>> fromStep = mock(org.jooq.SelectJoinStep.class);
        @SuppressWarnings("unchecked")
        org.jooq.SelectConditionStep<Record1<Long>> whereStep = mock(org.jooq.SelectConditionStep.class);
        when(dsl.select(RECORD.RECORD_OWNER_ID)).thenReturn(selectStep);
        when(selectStep.from(RECORD)).thenReturn(fromStep);
        when(fromStep.where(RECORD.ID.eq(3L))).thenReturn(whereStep);
        when(whereStep.fetchOne()).thenReturn(null);

        Mono<AbacContext> result = abacService.canModifyRecord(1L, 3L);

        StepVerifier.create(result)
                .assertNext(ctx -> assertEquals(AbacDecision.NOT_FOUND, ctx.decision()))
                .verifyComplete();
    }
}
