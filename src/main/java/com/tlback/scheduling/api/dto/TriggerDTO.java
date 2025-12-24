package com.tlback.scheduling.api.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerDTO {
    private String triggerName;
    private String triggerGroup;
    private String cronExpression;
    private String timeZone;
    private Date nextFireTime;
    private Date previousFireTime;
}
