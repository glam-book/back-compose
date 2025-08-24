package com.tlback.core.config.security.tg;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
class TelegramAuthConverter implements ServerAuthenticationConverter {

    private final TelegramAuthService telegramAuthService;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        var authentication = telegramAuthService.auth(exchange);
        return Mono.justOrEmpty(authentication);
    }

}
