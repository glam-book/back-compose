package com.tlback.config.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.tlback.domain.DomainUserEntity;

import lombok.ToString;

@ToString
public class UserData implements Authentication {
    private static final long serialVersionUID = 1L;

    private final DomainUserEntity details;
    private final Collection<? extends GrantedAuthority> authorities;
    private boolean isAuthenticated;
    private String password;

    public UserData(DomainUserEntity details,
        Collection<? extends GrantedAuthority> authorities) {
        this.details = details;
        this.authorities = authorities;
        isAuthenticated = true;
    }

    @Override
    public String getName() {
        return details.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public DomainUserEntity getDetails() {
        return details;
    }

    @Override
    public Long getPrincipal() {
        return details.getId();
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }
    
}
