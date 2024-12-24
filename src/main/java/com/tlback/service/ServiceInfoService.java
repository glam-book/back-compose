package com.tlback.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.dao.jooq.JooqServiceInfoRepository;
import com.tlback.domain.ServiceInfoEntity;
import com.tlback.domain.view.ServiceInfoView;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ServiceInfoService {
    private final JooqServiceInfoRepository jooqServiceInfoRepository;
    
    @Transactional(readOnly = true)
    public Flux<ServiceInfoEntity> findAllByUserId(Long userId) {
        return jooqServiceInfoRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Flux<ServiceInfoView> findAllViewByUserId(Long userId) {
        return jooqServiceInfoRepository.findAllViewByUserId(userId);
    }
}
