package com.tlback.domain;

import org.springframework.data.relational.core.mapping.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordPendingPk {
    
    @Column("client_id")
    private Long clientId;

    @Column("record_id")
    private Long recordId;
}
