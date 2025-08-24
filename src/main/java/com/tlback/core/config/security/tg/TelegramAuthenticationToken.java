package com.tlback.core.config.security.tg;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.tlback.core.model.TelegramUser;

public class TelegramAuthenticationToken implements Authentication {
    private final String hash;
    private final transient TelegramUser telegramUser;

    public TelegramAuthenticationToken(String hash, TelegramUser telegramUser) {
        this.hash = hash;
        this.telegramUser = telegramUser;
    }

    @Override
    public String getName() {
        return telegramUser.getFirstName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                () -> "ROLE_USER");
    }

    @Override
    public String getCredentials() {
        return hash;
    }

    @Override
    public TelegramUser getDetails() {
        return telegramUser;
    }

    @Override
    public Long getPrincipal() {
        return telegramUser.getId();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Unimplemented method 'setAuthenticated'");
    }
}
