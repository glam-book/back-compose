package com.tlback.scheduling.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCronRequestDTO {
    @NotBlank(message = "Cron expression cannot be blank")
    private String cronExpression;
}
