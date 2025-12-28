package com.tlback.app.rest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.app.dto.records.DeleteSuccess;
import com.tlback.app.dto.service.OptionalServiceInfoDto;
import com.tlback.domain.config.security.UserData;
import com.tlback.domain.mapper.ServiceInfoMapper;
import com.tlback.domain.service.ServiceInfoService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/v1/service")
@RequiredArgsConstructor
public class ServiceControllerV1 {
    private final ServiceInfoService service;
    private final ServiceInfoMapper mapper;

    @GetMapping("/list/{userId}")
    public Flux<OptionalServiceInfoDto> list(@PathVariable Long userId) {
        return service.findAllByUserId(userId).map(it -> mapper.toDto(it));
    }

    @PostMapping
    public Mono<OptionalServiceInfoDto> saveOrUpdate(
            @AuthenticationPrincipal UserData userData,
            @RequestBody OptionalServiceInfoDto serviceInfo) {
        return service.saveOrUpdate(serviceInfo, userData.getPrincipal()).map(it -> mapper.toDto(it));
    }

    @DeleteMapping("/{id}")
    public Mono<DeleteSuccess> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserData userData) {
        return service.deleteCascadeWithRecords(id, userData.getPrincipal())
            .map(it -> new DeleteSuccess(it));
    }
    
    
}
