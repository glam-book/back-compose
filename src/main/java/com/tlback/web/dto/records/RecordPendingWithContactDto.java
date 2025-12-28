package com.tlback.web.dto.records;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tlback.domain.model.contact.ContactProvider;
import com.tlback.web.dto.service.preview.ServicePreviewDto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordPendingWithContactDto<U extends ContactProvider> {
    private U contact; // TODO List ?
    private LocalDateTime requestTime;
    private Boolean confirmed;
    private Set<ServicePreviewDto> services; 
}
