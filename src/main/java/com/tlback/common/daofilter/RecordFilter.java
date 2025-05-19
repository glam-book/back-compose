package com.tlback.common.daofilter;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RecordFilter {
    private Long serviceInfoId;
    private Long recordOwnerId;
    private Long clientId;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private Boolean isPublic;
}