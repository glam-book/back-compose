package com.tlback.web.dto.records;

import java.time.LocalDateTime;
import java.util.Set;

import com.tlback.web.dto.service.preview.ServicePreviewDto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecordPendingDto implements Comparable<RecordPendingDto> {

    private Long requesterId;
    private String requesterLogin;
    private LocalDateTime requestTime;
    private Boolean confirmed;
    private Set<ServicePreviewDto> services;

    @Override
    public int compareTo(RecordPendingDto o) {
        if (this.requestTime == null || o.getRequestTime() == null)
            return 0;
        return this.requestTime.compareTo(o.getRequestTime());
    }
}