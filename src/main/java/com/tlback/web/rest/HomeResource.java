package com.tlback.web.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.config.security.UserData;
import com.tlback.domain.view.ServiceInfoView;
import com.tlback.service.RecordService;
import com.tlback.service.ServiceInfoService;
import com.tlback.service.UserService;
import com.tlback.web.dto.ServiceOwnerRecordDto;
import com.tlback.web.dto.UserDto;
import com.tlback.web.mapper.RecordMapper;
import com.tlback.web.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
@RequestMapping("/api")
public class HomeResource {
    private final UserService userService;
    private final UserMapper mapper;

    private final ServiceInfoService serviceInfo;

    private final RecordService recordService;
    private final RecordMapper recordMapper;

    @GetMapping("/get-user")
    public Mono<UserDto> getRecords(@RequestParam(name = "user_id") Long userId, UserData userData) {
        log.info("requested get user with user data: {}", userData);
        return userService.findById(userId).map(it -> mapper.toDto(it));
    }

    @GetMapping("/get-owner-records")
    public Flux<ServiceOwnerRecordDto> getOwnerRecords(
            @RequestParam(name = "user_id") Long userId, /*TODO get id from token*/
            @RequestParam(name = "time_from", required = false) LocalDateTime timeFrom,
            @RequestParam(name = "time_to", required = false) LocalDateTime timeTo,
            @RequestParam(name = "tz", required = false) ZoneOffset tz,
            @RequestParam(name = "date", required = false) LocalDate date) {

        if (date != null) {
            timeFrom = date.atTime(LocalTime.of(0, 0, 0));
            timeTo = timeFrom.plusDays(1);
        }

        return recordService.getOwnerRecords(userId, timeFrom, timeTo)
            .map(it -> recordMapper.toDto(it, tz));
    }

    @GetMapping("/get-service-info")
    public Flux<ServiceInfoView> getServiceInfo(@RequestParam(name = "user_id") Long userId) {
        return serviceInfo.findAllViewByUserId(userId);
    }

    @GetMapping("/client/create-record")
    public String recordClient() {
        return "\"Hello World!\"";
    }

    @GetMapping("/get-profile")
    public Mono<UserDto> getProfile(UserData token) {
        var userId = token.getPrincipal();
        return userService.findById(userId)
            .log().map(it -> {
                var dto = mapper.toDto(it);
                log.info("recevied user: {}", dto);
                return dto;
            });
    }

}
