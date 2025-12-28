package com.tlback.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.tlback.domain.model.utils.RecordSupplier;
import com.tlback.domain.model.utils.ServiceOwneraAware;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(schema = "public", name = "service_info")
public class ServiceInfoEntity implements RecordSupplier, ServiceOwneraAware, Serializable {

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

    @Column("time_duration")
    private Integer timeDuration;

    @Column("url")
    private String url;

    @Column("icon")
    private String icon;

    @Column("price")
    private BigDecimal price;

    @Column("is_hourly_price")
    private Boolean isHourlyPrice;

    @Column("service_permissions")
    private byte[] servicePermissions;

    private DomainUserEntity serviceOwner;

    private List<RecordEntity> records = new ArrayList<>();
}
