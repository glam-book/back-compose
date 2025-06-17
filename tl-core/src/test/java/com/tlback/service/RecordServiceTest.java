package com.tlback.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tlback.core.abac.AbacContext;
import com.tlback.core.abac.AbacService;
import com.tlback.core.dao.jooq.JooqRecordRepository;
import com.tlback.core.model.RecordEntity;
import com.tlback.core.service.RecordService;
import com.tlback.core.service.ServiceInfoService;
import com.tlback.core.web.dto.records.OptionalRecordCreateOrUpdateRequest;
import com.tlback.core.web.dto.service.OptionalServiceInfoDto;
import com.tlback.jooq.gen.tables.records.RecordRecord;
import com.tlback.jooq.gen.tables.records.ServiceInfoRecord;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RecordServiceTest {

    private JooqRecordRepository recordRepository;
    private ServiceInfoService serviceInfoService;
    private AbacService abac;
    private RecordService recordService;

    @BeforeEach
    void setUp() {
        recordRepository = mock(JooqRecordRepository.class);
        serviceInfoService = mock(ServiceInfoService.class);
        abac = mock(AbacService.class);
        recordService = new RecordService(recordRepository, serviceInfoService, abac);
    }

    @Test
    void saveOrUpdate_shouldCreateNewRecord_whenIdIsEmpty() {
        Long userId = 1L;
        OptionalRecordCreateOrUpdateRequest cmd = new OptionalRecordCreateOrUpdateRequest();
        cmd.setServiceInfo(new OptionalServiceInfoDto());
        // id пустой, значит создаём новую запись

        RecordEntity savedEntity = new RecordEntity();

        when(serviceInfoService.saveOrUpdate(any(), eq(userId)))
            .thenReturn(Mono.just(mock(ServiceInfoRecord.class))); // сервис не важен

        when(recordRepository.save(any(RecordRecord.class), any())).thenReturn(Mono.just(savedEntity));

        StepVerifier.create(recordService.saveOrUpdate(cmd, userId))
                .expectNext(savedEntity)
                .verifyComplete();

        verify(recordRepository).save(any(RecordRecord.class), any());
        verify(recordRepository, never()).update(any(), any());
    }

    @Test
    void saveOrUpdate_shouldUpdateRecord_whenIdIsPresentAndAbacAllows() {
        Long userId = 1L;
        Long recordId = 42L;
        OptionalRecordCreateOrUpdateRequest cmd = new OptionalRecordCreateOrUpdateRequest();
        cmd.setId(Optional.of(recordId));
        cmd.setServiceInfo(new OptionalServiceInfoDto());

        RecordEntity updatedEntity = new RecordEntity();
        when(serviceInfoService.saveOrUpdate(any(), eq(userId)))
            .thenReturn(Mono.just(mock(ServiceInfoRecord.class))); // сервис не важен

        when(abac.canModifyRecord(userId, recordId)).thenReturn(Mono.just(AbacContext.allow()));
        when(recordRepository.update(any(RecordRecord.class), any())).thenReturn(Mono.just(updatedEntity));

        StepVerifier.create(recordService.saveOrUpdate(cmd, userId))
                .expectNext(updatedEntity)
                .verifyComplete();

        verify(recordRepository).update(any(RecordRecord.class), any());
        verify(recordRepository, never()).save(any(), any());
    }

    @Test
    void saveOrUpdate_shouldError_whenIdIsPresentAndAbacDenies() {
        Long userId = 1L;
        Long recordId = 42L;
        OptionalRecordCreateOrUpdateRequest cmd = new OptionalRecordCreateOrUpdateRequest();
        cmd.setId(Optional.of(recordId));
        cmd.setServiceInfo(new OptionalServiceInfoDto());

        when(serviceInfoService.saveOrUpdate(any(), eq(userId)))
            .thenReturn(Mono.just(mock(ServiceInfoRecord.class))); // сервис не важен

        when(abac.canModifyRecord(userId, recordId)).thenReturn(Mono.just(AbacContext.deny("forbidden")));

        StepVerifier.create(recordService.saveOrUpdate(cmd, userId))
                .expectError()
                .verify();

        verify(recordRepository, never()).update(any(), any());
        verify(recordRepository, never()).save(any(), any());
    }
}