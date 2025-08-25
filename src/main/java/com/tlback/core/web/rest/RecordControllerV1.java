package com.tlback.core.web.rest;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.core.config.security.UserData;
import com.tlback.core.mapper.RecordMapper;
import com.tlback.core.service.RecordService;
import com.tlback.core.web.dto.records.OptionalRecordCreateOrUpdateRequest;
import com.tlback.core.web.dto.records.RecordPreviewResponse;
import com.tlback.core.web.dto.records.preview.RecordPendingsServiceResponsePreviewDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/record")
@RequiredArgsConstructor
public class RecordControllerV1 {
    private final RecordService recordService;
    private final RecordMapper recordMapper;

    @GetMapping("/list/{userId}")
    public Flux<RecordPendingsServiceResponsePreviewDto> list(UserData userDetail,
            @PathVariable Long userId, @RequestParam LocalDate date) {
        var details = userDetail.getDetails();
        var isOwner = details.getId().equals(userId);
        return recordService.getRecordsWithPendingsAndServiceByUserdId(userId, date)
                .map(it -> recordMapper.toDto(it, isOwner));
    }

    @PostMapping
    public Mono<RecordPreviewResponse> createRecord(
            @RequestBody OptionalRecordCreateOrUpdateRequest request,
            @AuthenticationPrincipal UserData userData) {
        System.out.println("Save or update request: " + request.toString());
        var owner = userData.getDetails().getId();
        return recordService.saveOrUpdate(request, owner)
                .map(it -> {
                    System.out.println("Saved record: " + it.toString());
                    return recordMapper.toPreviewResponse(it);
                });
    }
}