package com.tlback.domain.view;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.tlback.domain.DomainUserEntity;
import com.tlback.domain.RecordEntity;
import com.tlback.domain.utils.RecordSupplier;
import com.tlback.domain.utils.ServiceOwneraAware;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@Table("service_info")
public class ServiceInfoView implements RecordSupplier, ServiceOwneraAware {
    
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

    private List<RecordEntity> records = new ArrayList<>();

    @Override
    public void setServiceOwner(DomainUserEntity user) { }

    @Override
    public DomainUserEntity getServiceOwner() {
        return null;
    }
}
