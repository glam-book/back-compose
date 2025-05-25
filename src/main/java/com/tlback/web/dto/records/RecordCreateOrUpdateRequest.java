package com.tlback.web.dto.records;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tlback.web.dto.service.ServiceInfoDto;

import lombok.Data;

@Data
public class RecordCreateOrUpdateRequest {

    @JsonProperty("ts_from")
    private LocalDateTime tsFrom;

    @JsonProperty("ts_to")
    private LocalDateTime tsTo;

    @JsonProperty("service_info")
    private ServiceInfoDto serviceInfo;

}
