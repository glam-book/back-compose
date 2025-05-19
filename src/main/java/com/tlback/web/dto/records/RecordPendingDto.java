package com.tlback.web.dto.records;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecordPendingDto implements Comparable<RecordPendingDto> {

    @JsonProperty(value = "requester_id")
    private Long requesterId;

    @JsonProperty(value = "requester_login")
    private String requesterLogin;

    @JsonProperty(value = "request_time")
    private LocalDateTime requestTime;

    @JsonProperty(value = "confirmed")
    private Boolean confirmed;

    @Override
    public int compareTo(RecordPendingDto o) {
        if (this.requestTime == null || o.getRequestTime() == null)
            return 0;
        return this.requestTime.compareTo(o.getRequestTime());
    }
}