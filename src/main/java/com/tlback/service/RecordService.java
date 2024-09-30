package com.tlback.service;

import java.util.Collection;

import com.tlback.domain.Record;
import com.tlback.domain.ServiceOwnerInfo;

public interface RecordService {
    Record createRecord(ServiceOwnerInfo serviceOwnerInfo);

    Collection<Record> getAllRecords(Long serviceId);
}
