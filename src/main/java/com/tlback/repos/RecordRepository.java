package com.tlback.repos;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tlback.domain.Record;

public interface RecordRepository extends JpaRepository<Record, Long> {

    @Query("select r from Record r where r.serviceInfo.id = ?1")
    Collection<Record> findAllByServiceInfoId(Long serviceId);
}
