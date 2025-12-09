package com.tlback.scheduling.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private String jobName;
    private String jobGroup;
    private String jobClass;
    private List<TriggerDTO> triggers;
}
