package com.tlback.core.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.core.mapper.ServiceInfoMapper;
import com.tlback.core.service.ServiceInfoService;
import com.tlback.core.web.dto.service.OptionalServiceInfoDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/service")
@RequiredArgsConstructor
public class ServiceControllerV1 {
    private final ServiceInfoService service;
    private final ServiceInfoMapper mapper;

    @GetMapping("/list/{userId}")
    public Flux<OptionalServiceInfoDto> getMethodName(@PathVariable Long userId) {
        return service.findAllByUserId(userId).map(it -> mapper.toDto(it));
    }
    
}
