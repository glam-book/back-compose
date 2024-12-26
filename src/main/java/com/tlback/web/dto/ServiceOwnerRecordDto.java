package com.tlback.web.dto;

import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOwnerRecordDto {

    private Long id;

    @JsonProperty(value = "service_info_id")
    private Long serviceInfoId;

    @JsonProperty(value = "record_owner_id")
    private Long recordOwnerId;

    @JsonProperty(value = "is_public")
    private Boolean isPublic;

    @JsonProperty(value = "time_from")
    private Long timeFrom;

    @JsonProperty(value = "time_to")
    private Long timeTo;

    @JsonProperty(value = "record_pendings")
    private SortedSet<OwnerRecordPendingDto> recordPendings;
}
