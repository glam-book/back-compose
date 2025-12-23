package com.tlback.web.dto.records;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tlback.web.dto.service.OptionalServiceInfoDto;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptionalRecordCreateOrUpdateRequest {
    private Optional<Long> id = Optional.empty();
    private LocalDateTime tsFrom;
    private LocalDateTime tsTo;
    private String comment;
    private List<OptionalServiceInfoDto> serviceInfo;
}
