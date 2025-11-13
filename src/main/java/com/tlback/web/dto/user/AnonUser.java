package com.tlback.web.dto.user;

import lombok.Builder;

@Builder
public record AnonUser(String identity) {
}
