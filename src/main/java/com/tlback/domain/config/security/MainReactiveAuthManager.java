package com.tlback.domain.config.security;

import java.util.List;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.tlback.domain.abac.exception.NotFoundException;
import com.tlback.domain.config.security.tg.TelegramAuthenticationToken;
import com.tlback.domain.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class MainReactiveAuthManager implements ReactiveAuthenticationManager {
    private final UserService userService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (authentication instanceof TelegramAuthenticationToken tgToken) {
            var tgUser = tgToken.getDetails();
            return userService.findByTgId(tgUser.getId())
                    .onErrorResume(NotFoundException.class, it -> userService.createFromTgUser(tgUser))
                    .map(it -> new UserData(it, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        }

        return Mono.error(new UnsupportedOperationException());
    }

}