package com.tlback.web.dto.records.preview;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordPendingsServiceResponsePreviewDto {

    private Long id;

    @JsonProperty(value = "service_info")
    private ServicePreviewDto serviceInfo;

    @JsonProperty(value = "ts_from")
    private LocalDateTime tsFrom;

    @JsonProperty(value = "ts_to")
    private LocalDateTime tsTo;

    @JsonProperty(value = "pendings")
    private RecordPendingPreviewDto recordPendings;

}