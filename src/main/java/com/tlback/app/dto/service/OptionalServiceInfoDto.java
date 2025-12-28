package com.tlback.app.dto.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptionalServiceInfoDto {

    private Optional<Long> id = Optional.empty();
    private String title;
    private String url;
    private String description;
    private String icon;
    private BigDecimal price;
    private Boolean isHourlyPrice;
}
