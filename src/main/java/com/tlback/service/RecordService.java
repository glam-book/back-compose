package com.tlback.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.abac.AbacService;
import com.tlback.common.daofilter.RecordFilter;
import com.tlback.dao.jooq.JooqRecordRepository;
import com.tlback.jooq.gen.tables.records.RecordRecord;
import com.tlback.model.RecordEntity;
import com.tlback.tools.ZoneOffsetTools;
import com.tlback.web.dto.records.OptionalRecordCreateOrUpdateRequest;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final JooqRecordRepository recordRepository;
    private final ServiceInfoService serviceInfoService;
    private final AbacService abac;

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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Cacheable(value = "records", key = "{#userId, #date}")
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

    @Transactional
    @CachePut(value = "record", key = "#userId")
    public Mono<RecordEntity> saveOrUpdate(OptionalRecordCreateOrUpdateRequest cmd, Long userId) {
        var joinService = JooqRecordRepository.JOIN_SERVICE_INFO;
        var serviceRequest = cmd.getServiceInfo();
        var service = serviceInfoService.saveOrUpdate(serviceRequest, userId);

        return service.flatMap(serviceMono -> cmd.getId()
                // if record id exists - check access and update record if allowed
                .map(recId -> abac.canModifyRecord(userId, recId)
                        // TODO block hooligan user
                        .flatMap(abacResult -> abacResult.mapResult(
                                () -> recordRepository.update(mapToRecord(cmd, recId, userId), joinService), 
                                Mono::error)))
                // or else create new one
                .orElseGet(() -> recordRepository.save(mapToRecord(cmd, userId, userId), joinService)));
    }

    private RecordRecord mapToRecord(OptionalRecordCreateOrUpdateRequest cmd, Long serviceId, Long userId) {
        var newRecord = new RecordRecord();
        cmd.getId().ifPresent(newRecord::setId);
        newRecord.setTz(ZoneOffsetTools.DEFAULT_OFFSET); // TODO get from client in future
        newRecord.setIsPublic(true);
        newRecord.setTsFrom(cmd.getTsFrom());
        newRecord.setTsTo(cmd.getTsTo());
        newRecord.setServiceInfoId(serviceId);
        newRecord.setRecordOwnerId(userId);
        return newRecord;
    }
}
