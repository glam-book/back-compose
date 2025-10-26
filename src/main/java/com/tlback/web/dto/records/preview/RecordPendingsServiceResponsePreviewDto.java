package com.tlback.web.dto.records.preview;

import java.time.LocalDateTime;
import java.util.List;

import com.tlback.web.dto.service.preview.ServicePreviewDto;

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
    private List<ServicePreviewDto> serviceInfo;
    private LocalDateTime tsFrom;
    private LocalDateTime tsTo;
    private String comment;
    private RecordPendingPreviewDto recordPendings;
}