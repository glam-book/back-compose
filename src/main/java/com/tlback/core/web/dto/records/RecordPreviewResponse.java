package com.tlback.core.web.dto.records;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RecordPreviewResponse {
    private Long id;
    private LocalDateTime tsFrom;
    private LocalDateTime tsTo;
    private String comment;
    private Long serviceInfoId;
}
