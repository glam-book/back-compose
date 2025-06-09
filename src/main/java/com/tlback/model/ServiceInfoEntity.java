package com.tlback.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.tlback.model.utils.RecordSupplier;
import com.tlback.model.utils.ServiceOwneraAware;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(schema = "public", name = "service_info")
public class ServiceInfoEntity implements RecordSupplier, ServiceOwneraAware {

    @Id
    @Column("id")
    private Long id;

    @Column("service_owner_id")
    private Long serviceOwnerId;

    @Column("service_name")
    private String serviceName;

    @Column("editable")
    private Boolean editable;

    @Column("service_description")
    private String serviceDescription;

    @Column("record_limit")
    private Integer recordLimit;

    @Column("time_duration")
    private Integer timeDuration;

    private DomainUserEntity serviceOwner;

    private List<RecordEntity> records = new ArrayList<>();
}
