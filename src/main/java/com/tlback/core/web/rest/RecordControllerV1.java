package com.tlback.core.web.rest;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tlback.core.config.security.UserData;
import com.tlback.core.mapper.RecordMapper;
import com.tlback.core.model.RecordEntity;
import com.tlback.core.service.RecordService;
import com.tlback.core.web.dto.records.DeleteSuccess;
import com.tlback.core.web.dto.records.OptionalRecordCreateOrUpdateRequest;
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
                .map(it -> map(it, isOwner, userId));
    }

    @PostMapping
    public Mono<RecordPendingsServiceResponsePreviewDto> createRecord(
            @RequestBody OptionalRecordCreateOrUpdateRequest request,
            @AuthenticationPrincipal UserData userData) {
        var owner = userData.getDetails().getId();
        return recordService.saveOrUpdate(request, owner)
                .map(it -> {
                    return recordMapper.toDto(it, false, true);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<DeleteSuccess> deleteRecord(
            @PathVariable Long id,
            @AuthenticationPrincipal UserData userData) {
        var owner = userData.getDetails().getId();
        var result = recordService.deleteCascadeWithPendings(id, owner);
        return result.map(it -> new DeleteSuccess(it));
    }

    @PutMapping("/pending/{recordId}")
    public Mono<RecordPendingsServiceResponsePreviewDto> craetePending(UserData userDetail,
            @PathVariable Long recordId) {
        var details = userDetail.getDetails();
        var userId = userDetail.getPrincipal();
        var isOwner = details.getId().equals(userId);

        return recordService.createPending(userId, recordId)
            .map(it -> map(it, isOwner, userId));
    }

    private RecordPendingsServiceResponsePreviewDto map(RecordEntity entity, boolean isOwner, Long userId) {
        var hasPendings = entity.getRecordPendings() != null &&
                entity.getRecordPendings().stream()
                        .anyMatch(pending -> pending.getClientId().equals(userId));
        return recordMapper.toDto(entity, !(hasPendings && isOwner), isOwner);
    }
}