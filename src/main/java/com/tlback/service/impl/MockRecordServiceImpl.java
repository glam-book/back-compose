package com.tlback.service.impl;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tlback.domain.Record;
import com.tlback.domain.ServiceOwnerInfo;
import com.tlback.domain.User;
import com.tlback.repos.RecordRepository;
import com.tlback.service.RecordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MockRecordServiceImpl implements RecordService {
    private final User user;
    private final RecordRepository rep;

    @Override
    public Record createRecord(ServiceOwnerInfo serviceOwnerInfo) {
        var rec = new Record();
        rec.setRecordOwner(user);
        rec.setServiceInfo(serviceOwnerInfo);
        rec.setIsPublic(true);
        rec.setTimeFrom(OffsetDateTime.now());
        rec.setTimeTo(OffsetDateTime.now());
        return rep.save(rec);
    }

    @Override
    public List<Record> getAllRecords(Long serviceId) {
        return rep.findAllByServiceInfoId(serviceId)
            .stream().toList();
    }
}
