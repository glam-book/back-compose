package com.tlback.domain;

import java.time.OffsetDateTime;
import java.util.Set;

import jakarta.persistence.CascadeType;
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
@Table(name = "record")
@Getter
@Setter
public class Record {

    @Id
    @Column(nullable = false, updatable = false, name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "time_from")
    private OffsetDateTime timeFrom;

    @Column(name = "time_to")
    private OffsetDateTime timeTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_info_id")
    private ServiceOwnerInfo serviceInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_owner_id")
    private User recordOwner;

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RecordPending> pendingList;

}
