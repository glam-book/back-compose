package com.tlback.web.rest;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.domain.view.ServiceInfoView;
import com.tlback.service.ServiceInfoService;
import com.tlback.service.UserService;
import com.tlback.web.dto.UserDto;
import com.tlback.web.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class HomeResource {
    private final UserService userService;
    private final UserMapper mapper;
    private final ServiceInfoService serviceInfo;

    @PostMapping("/create")
    public Mono<UserDto> postMethodName(@RequestBody UserDto userDto) {
        log.info("accpeting create request: {}" + userDto);
        return userService.createUser(mapper.toEntity(userDto)).map(it -> mapper.toDto(it));
    }

    @GetMapping("/get-user")
    public Mono<UserDto> getRecords(@RequestParam(name = "user_id") Long userId) {
        return userService.findById(userId).map(it -> mapper.toDto(it));
    }

    @GetMapping("/get-service-info")
    public Flux<ServiceInfoView> getServiceInfo(@RequestParam(name = "user_id") Long userId) {
        return serviceInfo.findAllViewByUserId(userId);
    }

    @GetMapping("/client/create-record")
    public String recordClient() {
        return "\"Hello World!\"";
    }

}
