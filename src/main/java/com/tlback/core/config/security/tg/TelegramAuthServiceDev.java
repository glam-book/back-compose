package com.tlback.core.config.security.tg;

import java.util.Optional;

import org.springframework.web.server.ServerWebExchange;

import com.tlback.core.model.TelegramUser;

public class TelegramAuthServiceDev extends TelegramAuthService {

    public TelegramAuthServiceDev(String botToken) {
        super(botToken);
    }

    private Optional<TelegramAuthenticationToken> authMock() {
        var telegramUser = new TelegramUser();
        telegramUser.setUserId(1L);
        telegramUser.setId(100L);
        telegramUser.setFirstName("Igor");
        telegramUser.setLastName("Abobenko");
        var auth = new TelegramAuthenticationToken("hash", telegramUser);
        return Optional.of(auth);
    }

    @Override
    public Optional<TelegramAuthenticationToken> auth(ServerWebExchange exchange) {
        return authMock();
    }
}
