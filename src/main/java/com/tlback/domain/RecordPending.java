package com.tlback.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table("record_pending")
public class RecordPending {

    @Id
    private RecordPendingPk pk;

    @Column("request_time")
    private Long requestTime;

    @Column("confirmed")
    private Boolean confirmed;
}