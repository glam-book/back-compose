package com.tlback.web.dto.records;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RecordPreviewResponse {
    private Long id;
    private LocalDateTime tsFrom;
    private LocalDateTime tsTo;
    private Integer serviceInfoId;
}
