package com.tlback.web.dto.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ServiceInfoDto {
    private String title;
    private String url;
    private String description;
    private String icon;
    @JsonProperty("record_limit")
    private Integer recordLimit = 1;
}
