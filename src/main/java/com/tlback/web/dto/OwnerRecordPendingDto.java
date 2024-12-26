package com.tlback.web.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OwnerRecordPendingDto implements Comparable<OwnerRecordPendingDto> {

    @JsonProperty(value = "requester_id")
    private Long requesterId;

    @JsonProperty(value = "requester_login")
    private String requesterLogin;

    @JsonProperty(value = "request_time")
    private LocalDateTime requestTime;

    @JsonProperty(value = "confirmed")
    private Boolean confirmed;

    @Override
    public int compareTo(OwnerRecordPendingDto o) {
        if (this.requestTime == null || o.getRequestTime() == null)
            return 0;
        return this.requestTime.compareTo(o.getRequestTime());
    }
}