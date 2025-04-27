package com.tlback.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.common.daofilter.RecordFilter;
import com.tlback.dao.jooq.JooqRecordRepository;
import com.tlback.domain.RecordEntity;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final JooqRecordRepository recordRepository;

    @Transactional(readOnly = true)
    public Flux<RecordEntity> getRecords(RecordFilter filter) {
        return recordRepository.findByFilter(filter);
    }

    @Transactional(readOnly = true)
    public Flux<RecordEntity> getOwnerRecords(Long userId, LocalDateTime timeFrom, LocalDateTime timeTo) {

        var filter = RecordFilter.builder().recordOwnerId(userId).dateFrom(timeFrom).dateTo(timeTo).build();
        return recordRepository.findByFilter(filter);
    }

}
