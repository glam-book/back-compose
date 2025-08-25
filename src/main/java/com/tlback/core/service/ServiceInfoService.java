package com.tlback.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.core.abac.AbacService;
import com.tlback.core.dao.jooq.JooqServiceInfoRepository;
import com.tlback.core.model.ServiceInfoEntity;
import com.tlback.core.web.dto.service.OptionalServiceInfoDto;
import com.tlback.jooq.gen.tables.records.ServiceInfoRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceInfoService {
    private final JooqServiceInfoRepository jooqServiceInfoRepository;
    private final AbacService abac;

    @Transactional(readOnly = true)
    public Flux<ServiceInfoEntity> findAllByUserId(Long userId) {
        return jooqServiceInfoRepository.findAllByUserId(userId);
    }

    public Mono<ServiceInfoRecord> saveOrUpdate(OptionalServiceInfoDto dto, Long userId) {
        log.info("Save or update service request: {}", dto.toString());

        return dto.getId()
        //@formatter:off
            .map(id -> abac.canUseService(userId, id)
                .flatMap(abacResult -> abacResult
                    .mapResult(
                        () -> jooqServiceInfoRepository.update(mapInfoRecord(dto, userId)),
                        Mono::error)))
            //@formatter:on
                .orElseGet(() -> jooqServiceInfoRepository.save(mapInfoRecord(dto, userId)));
    }

    private ServiceInfoRecord mapInfoRecord(OptionalServiceInfoDto dto, Long userId) {
        var rec = new ServiceInfoRecord();
        dto.getId().ifPresent(rec::setId);
        rec.setServiceName(dto.getTitle());
        rec.setEditable(false);
        rec.setRecordLimit(dto.getRecordLimit());
        rec.setServiceDescription(dto.getDescription());
        rec.setServiceOwnerId(userId);
        return rec;
    }

}
