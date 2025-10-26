package com.tlback.core.model;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Column("record_owner_id")
    private Long recordOwnerId;

    @Column("is_public")
    private Boolean isPublic = true;

    @Column("ts_from")
    private OffsetDateTime tsFrom;

    @Column("ts_to")
    private OffsetDateTime tsTo;

    @Column("tz")
    private String tz;

    @Column("comment")
    private String comment;

    @Column("record_limit")
    private Integer recordLimit;

    @Column("record_permissions")
    private byte[] recordPermissions;

    private Set<RecordPending> recordPendings = new HashSet<>();

    private Set<ServiceInfoEntity> serviceInfo = new HashSet<>();
}
