package com.tlback.web.dto.service.preview;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServicePreviewDto {
    private Long id;
    private String title;
    private BigDecimal price;
    private Boolean isHourlyPrice;
}
