package com.tlback.core.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.core.config.security.UserData;
import com.tlback.core.mapper.UserMapper;
import com.tlback.core.service.UserService;
import com.tlback.core.web.dto.user.UserDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/info")
@RequiredArgsConstructor
public class InfoController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public Mono<UserDto> getMethodName(UserData userData) {
        return userService.findById(userData.getDetails().getId())
                .map(userMapper::toDto);
    }
}