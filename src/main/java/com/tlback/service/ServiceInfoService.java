package com.tlback.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.abac.AbacService;
import com.tlback.dao.jooq.JooqServiceInfoRepository;
import com.tlback.jooq.gen.tables.records.ServiceInfoRecord;
import com.tlback.model.ServiceInfoEntity;
import com.tlback.web.dto.service.OptionalServiceInfoDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ServiceInfoService {
    private final JooqServiceInfoRepository jooqServiceInfoRepository;
    private final AbacService abac;

    @Transactional(readOnly = true)
    public Flux<ServiceInfoEntity> findAllByUserId(Long userId) {
        return jooqServiceInfoRepository.findAllByUserId(userId);
    }

    public Mono<ServiceInfoRecord> saveOrUpdate(OptionalServiceInfoDto dto, Long userId) {
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
