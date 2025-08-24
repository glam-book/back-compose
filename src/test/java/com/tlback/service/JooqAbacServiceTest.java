package com.tlback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tlback.core.abac.AbacContext;
import com.tlback.core.abac.AbacDecision;
import com.tlback.core.abac.impl.JooqAbacServiceImpl;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JooqAbacServiceTest {
    private DSLContext dsl;
    private JooqAbacServiceImpl abacService;

    @BeforeEach
    void setUp() {
        dsl = mock(DSLContext.class);
        abacService = new JooqAbacServiceImpl(dsl);
    }

    @Test
    void testCanModifyRecord_asOwner() {
        JooqAbacServiceImpl abacServiceSpy = spy(abacService);
        @SuppressWarnings("unchecked")
        Record1<Long> mockedRecord = (Record1<Long>) mock(Record1.class);
        when(mockedRecord.value1()).thenReturn(42L);
        doReturn(Mono.just(mockedRecord)).when(abacServiceSpy).fetchRecordOwnerId(1L);

        Mono<AbacContext> result = abacServiceSpy.canModifyRecord(42L, 1L);

        StepVerifier.create(result)
                .assertNext(ctx -> assertTrue(ctx.isAllowed()))
                .verifyComplete();
    }

    @Test
    void testCanModifyRecord_notOwner() {
        JooqAbacServiceImpl abacServiceSpy = spy(abacService);
        @SuppressWarnings("unchecked")
        Record1<Long> mockedRecord = (Record1<Long>) mock(Record1.class);
        when(mockedRecord.value1()).thenReturn(42L);
        doReturn(Mono.just(mockedRecord)).when(abacServiceSpy).fetchRecordOwnerId(2L);

        Mono<AbacContext> result = abacServiceSpy.canModifyRecord(99L, 2L);

        StepVerifier.create(result)
                .assertNext(ctx -> {
                    assertFalse(ctx.isAllowed());
                    assertEquals(AbacDecision.DENY, ctx.decision());
                })
                .verifyComplete();
    }

    @Test
    void testCanModifyRecord_notFound() {
        JooqAbacServiceImpl abacServiceSpy = spy(abacService);
        doReturn(Mono.justOrEmpty(null)).when(abacServiceSpy).fetchRecordOwnerId(3L);

        Mono<AbacContext> result = abacServiceSpy.canModifyRecord(1L, 3L);

        StepVerifier.create(result)
                .assertNext(ctx -> assertEquals(AbacDecision.NOT_FOUND, ctx.decision()))
                .verifyComplete();
    }
}
