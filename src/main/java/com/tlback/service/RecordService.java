package com.tlback.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tlback.dao.jooq.JooqRecordRepository;
import com.tlback.domain.RecordEntity;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final JooqRecordRepository recordRepository;

    public Flux<RecordEntity> getOwnerRecords(Long userId) {
        return recordRepository.findByDateAndServiceOwner(userId, /*TODO */null, /*TODO */null,
            Optional.empty());
    }
}
