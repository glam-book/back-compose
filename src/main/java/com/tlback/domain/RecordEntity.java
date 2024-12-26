package com.tlback.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(schema = "public", name = "record")
public class RecordEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("service_info_id")
    private Long serviceInfoId;

    @Column("record_owner_id")
    private Long recordOwnerId;

    @Column("is_public")
    private Boolean isPublic;

    @Column("time_from")
    private Long timeFrom;

    @Column("time_to")
    private Long timeTo;

    private List<RecordPending> recordPendings = new ArrayList<>();
}
