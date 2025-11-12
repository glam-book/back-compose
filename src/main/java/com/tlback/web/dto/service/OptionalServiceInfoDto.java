package com.tlback.web.dto.service;

import java.math.BigDecimal;
import java.util.Optional;

import lombok.Data;

@Data
public class OptionalServiceInfoDto {

    private Optional<Long> id = Optional.empty();
    private String title;
    private String url;
    private String description;
    private String icon;
    private Integer recordLimit = 1;
    private BigDecimal price;
    private Boolean isHourlyPrice;
}
