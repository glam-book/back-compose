package com.tlback.core.web.dto.records.preview;

import java.time.LocalDateTime;

import com.tlback.core.web.dto.service.preview.ServicePreviewDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordPendingsServiceResponsePreviewDto {
    private Long id;
    private boolean pendigable;
    private boolean isOwner;
    private ServicePreviewDto serviceInfo;
    private LocalDateTime tsFrom;
    private LocalDateTime tsTo;
    private String comment;
    private RecordPendingPreviewDto recordPendings;
}