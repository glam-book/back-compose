package com.tlback.web.rest;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.config.security.UserData;
import com.tlback.service.RecordService;
import com.tlback.web.dto.records.OptionalRecordCreateOrUpdateRequest;
import com.tlback.web.dto.records.RecordPreviewResponse;
import com.tlback.web.dto.records.preview.RecordPendingsServiceResponsePreviewDto;
import com.tlback.web.mapper.RecordMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/record")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;
    private final RecordMapper recordMapper;

    @GetMapping
    public Flux<RecordPendingsServiceResponsePreviewDto> entity(UserData userDetail, @RequestParam LocalDate date) {
        var details = userDetail.getDetails();
        return recordService.getRecordsWithPendingsAndServiceByUserdId(details.getId(), date)
                .map(recordMapper::toDto);
    }

    @PostMapping
    public Mono<RecordPreviewResponse> createRecord(
            @RequestBody OptionalRecordCreateOrUpdateRequest request,
            @AuthenticationPrincipal UserData userData) {

        var owner = userData.getDetails().getId();
        return recordService.saveOrUpdate(request, owner)
                .map(recordMapper::toPreviewResponse);
    }
}