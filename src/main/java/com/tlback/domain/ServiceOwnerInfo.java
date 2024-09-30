package com.tlback.domain;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "service_owner_info")
@Getter
@Setter
public class ServiceOwnerInfo {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 75, name = "service_name")
    private String serviceName;

    @Column(columnDefinition = "text", name = "service_description")
    private String serviceDescription;

    @Column(name = "record_limit")
    private Integer recordLimit;

    @Column(name = "time_duration")
    private Integer timeDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_owner_id")
    private User serviceOwner;

    @OneToMany(mappedBy = "serviceInfo")
    private Set<Record> records;

    @OneToMany(mappedBy = "serviceInfo")
    private Set<RecordPending> pendingList;

}
