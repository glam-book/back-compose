package com.tlback.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.domain.config.security.UserData;
import com.tlback.domain.mapper.UserMapper;
import com.tlback.domain.service.UserService;
import com.tlback.web.dto.user.UserProfileDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserControllerV1 {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public Mono<UserProfileDto> getMethodName(UserData userData) {
        return userService.findById(userData.getDetails().getId())
                .map(userMapper::toProfileDto);
    }

    @GetMapping("/{userId}")
    public Mono<UserProfileDto> getProfileInfo(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(userMapper::toProfileDto);
    }
    
}
