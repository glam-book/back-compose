package com.tlback.web.rest;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.config.security.UserData;
import com.tlback.service.RecordService;
import com.tlback.web.dto.records.preview.RecordPendingsServiceResponsePreviewDto;
import com.tlback.web.mapper.RecordMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/record")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService service;
    private final RecordMapper recordMapper;

    @GetMapping
    public Flux<RecordPendingsServiceResponsePreviewDto> entity(UserData userDetail, @RequestParam LocalDate date) {
        var details = userDetail.getDetails();
        return service.getRecordsWithPendingsAndServiceByUserdId(details.getId(), date)
                .map(recordMapper::toDto);
    }
}