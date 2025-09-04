package com.tlback.core.web.dto.records;

import java.time.LocalDateTime;
import java.util.Optional;

import com.tlback.core.web.dto.service.OptionalServiceInfoDto;

import lombok.Data;

@Data
public class OptionalRecordCreateOrUpdateRequest {
    private Optional<Long> id = Optional.empty();
    private LocalDateTime tsFrom;
    private LocalDateTime tsTo;
    private String comment;
    private OptionalServiceInfoDto serviceInfo;
}
