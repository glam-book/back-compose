package com.tlback.web.dto.records;

import java.time.LocalDateTime;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tlback.web.dto.service.OptionalServiceInfoDto;

import lombok.Data;

@Data
public class OptionalRecordCreateOrUpdateRequest {

    @JsonProperty("id")
    private Optional<Long> id = Optional.empty();

    @JsonProperty("ts_from")
    private LocalDateTime tsFrom;

    @JsonProperty("ts_to")
    private LocalDateTime tsTo;

    @JsonProperty("service_info")
    private OptionalServiceInfoDto serviceInfo;

}
