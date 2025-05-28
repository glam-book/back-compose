package com.tlback.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.common.daofilter.RecordFilter;
import com.tlback.dao.jooq.JooqRecordRepository;
import com.tlback.dao.jooq.JooqServiceInfoRepository;
import com.tlback.model.RecordEntity;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final JooqRecordRepository recordRepository;

    @Transactional(readOnly = true)
    public Flux<RecordEntity> getRecords(RecordFilter filter) {
        return recordRepository.findFullByFilter(filter);
    }

    @Transactional(readOnly = true)
    public Flux<RecordEntity> getRecordsByUserId(Long userId, LocalDateTime timeFrom, LocalDateTime timeTo) {
        var filter = RecordFilter.builder()
                .recordOwnerId(userId)
                .dateFrom(timeFrom)
                .dateTo(timeTo).build();
        return recordRepository.findFullByFilter(filter);
    }

    // TODO add cache
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Flux<RecordEntity> getRecordsWithPendingsAndServiceByUserdId(Long userId, LocalDate date) {
        var fromDate = date.atStartOfDay();
        var toDate = date.plusDays(1).atStartOfDay();

        var filter = RecordFilter.builder()
                .recordOwnerId(userId)
                .dateFrom(fromDate)
                .dateTo(toDate).build();

        return recordRepository.findByFilter(filter,
                List.of(JooqRecordRepository.JOIN_SERVICE_INFO, JooqRecordRepository.JOIN_RECORD_PENDINGS));
    }

    /**
     * Создать или обновить запись (шаблон)
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<RecordEntity> createRecord(RecordEntity recordEntity, long userId) {
        return recordRepository.createNew(recordEntity, 
            JooqServiceInfoRepository.insertModuleId, 
            JooqRecordRepository.JOIN_SERVICE_INFO);
    }
}
