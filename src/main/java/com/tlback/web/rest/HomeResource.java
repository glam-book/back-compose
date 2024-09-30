package com.tlback.web.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.service.OwnerService;
import com.tlback.service.RecordService;
import com.tlback.web.dto.RecordDto;
import com.tlback.web.dto.ServiceOwnerInfoDto;
import com.tlback.web.mapper.RecordMapper;
import com.tlback.web.mapper.ServiceOwnerMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequiredArgsConstructor
@Slf4j
public class HomeResource {
    private final OwnerService ownerService;
    private final RecordService recordService;
    private final ServiceOwnerMapper ownerMapper;
    private final RecordMapper recordMapper;

    @GetMapping("/create-service")
    public ServiceOwnerInfoDto service(@RequestParam(name = "name") String name) {
        var service = ownerService.createService(name);
        log.info(service.toString());
        return ownerMapper.toDto(service);
    }

    @GetMapping("/owner/create-record")
    public RecordDto recordOwner(@RequestParam(name = "service_id") Long serviceId) {
        var service = ownerService.findServiceById(serviceId);
        return recordMapper.toDto(recordService.createRecord(service));
    }

    @GetMapping("/get-records")
    public List<RecordDto> getRecords(@RequestParam(name = "service_id") Long serviceId) {
        return recordService.getAllRecords(serviceId).stream()
            .map(recordMapper::toDto).toList();
    }
    

    @GetMapping("/client/create-record")
    public String recordClient() {
        return "\"Hello World!\"";
    }

}
