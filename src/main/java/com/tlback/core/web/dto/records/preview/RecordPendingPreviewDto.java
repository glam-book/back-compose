package com.tlback.core.web.dto.records.preview;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordPendingPreviewDto {
    private Integer limits;
    private Integer active;
}
