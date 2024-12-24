package com.tlback.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OwnerRecordPendingDto implements Comparable<OwnerRecordPendingDto> {

    @JsonProperty(value = "request_time")
    private Long requestTime;

    @JsonProperty(value = "confirmed")
    private Boolean confirmed;

    @JsonProperty(value = "requester_id")
    private Long requesterId;

    @JsonProperty(value = "requester_login")
    private String requesterLogin;

    @Override
    public int compareTo(OwnerRecordPendingDto o) {
        if (this.requestTime == null || o.getRequestTime() == null)
            return 0;
        return this.requestTime.compareTo(o.getRequestTime());
    }
}