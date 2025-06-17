package com.tlback.core.web.dto.service;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OptionalServiceInfoDto {

    @JsonProperty("id")
    private Optional<Long> id = Optional.empty();
    private String title;
    private String url;
    private String description;
    private String icon;
    @JsonProperty("record_limit")
    private Integer recordLimit = 1;
}
