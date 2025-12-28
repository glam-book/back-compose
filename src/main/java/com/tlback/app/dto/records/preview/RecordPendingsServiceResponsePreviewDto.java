package com.tlback.app.dto.records.preview;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tlback.app.dto.service.preview.ServicePreviewDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordPendingsServiceResponsePreviewDto {
    private Long id;
    private boolean pendigable;
    private boolean isOwner;
    private List<ServicePreviewDto> serviceInfo;
    private LocalDateTime tsFrom;
    private LocalDateTime tsTo;
    private String comment;
    private RecordPendingPreviewDto recordPendings;
}