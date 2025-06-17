package com.tlback.core.web.rest;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.core.config.security.UserData;
import com.tlback.core.service.RecordService;
import com.tlback.core.web.dto.records.OptionalRecordCreateOrUpdateRequest;
import com.tlback.core.web.dto.records.RecordPreviewResponse;
import com.tlback.core.web.dto.records.preview.RecordPendingsServiceResponsePreviewDto;
import com.tlback.core.web.mapper.RecordMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/record")
@RequiredArgsConstructor
public class RecordControllerV1 {
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