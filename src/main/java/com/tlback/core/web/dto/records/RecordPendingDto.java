package com.tlback.core.web.dto.records;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecordPendingDto implements Comparable<RecordPendingDto> {

    private Long requesterId;
    private String requesterLogin;
    private LocalDateTime requestTime;
    private Boolean confirmed;

    @Override
    public int compareTo(RecordPendingDto o) {
        if (this.requestTime == null || o.getRequestTime() == null)
            return 0;
        return this.requestTime.compareTo(o.getRequestTime());
    }
}