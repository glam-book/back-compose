package com.tlback.web.rest;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
import com.tlback.core.mapper.ContactMapper;
import com.tlback.core.mapper.RecordMapper;
import com.tlback.core.mapper.ServiceInfoMapper;
import com.tlback.core.model.RecordEntity;
import com.tlback.core.model.contact.Supports;
import com.tlback.core.service.RecordService;
import com.tlback.web.dto.records.DeleteSuccess;
import com.tlback.web.dto.records.OptionalRecordCreateOrUpdateRequest;
import com.tlback.web.dto.records.RecordCalendarDto;
import com.tlback.web.dto.records.RecordPendingWithContactDto;
import com.tlback.web.dto.records.preview.RecordPendingsServiceResponsePreviewDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/record")
@RequiredArgsConstructor
@Slf4j
public class RecordControllerV1 {
    private final RecordService recordService;
    private final RecordMapper recordMapper;
    private final ContactMapper contactMapper;
    private final ServiceInfoMapper serviceInfoMapper;

    @GetMapping("/list/{userId}")
    public Flux<RecordPendingsServiceResponsePreviewDto> list(UserData userDetail,
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate date) {
        var details = userDetail.getDetails();
        var isOwner = details.getId().equals(userId);
        return recordService.getRecordsWithPendingsAndServiceByUserdId(userId, date)
                .map(it -> mapRecord(it, isOwner, userDetail.getPrincipal()));
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

    @GetMapping("/pending/{recordId}")
    public Flux<RecordPendingWithContactDto<?>> pendingDetails(UserData userDetail,
            @PathVariable(name = "recordId") Long recordId,
            @RequestParam(required = false, defaultValue = "TG", name = "contactTarget") String contactTarget) {
        var userId = userDetail.getPrincipal();
        var supports = Supports.valueOf(contactTarget);
        return recordService.getPendingDetails(recordId, userId)
                .map(it -> RecordPendingWithContactDto
                        .builder()
                        .contact(contactMapper.of(supports, it.getPendingOwner()).getOrNull())
                        .services(it.getServices().stream().map(serviceInfoMapper::map).collect(Collectors.toSet()))
                        .requestTime(it.getRequestTime())
                        .confirmed(it.getConfirmed())
                        .build());
    }

    @PutMapping("/pending/{recordId}")
    public Mono<RecordPendingsServiceResponsePreviewDto> craetePending(UserData userDetail,
            @PathVariable Long recordId,
            @RequestBody Set<Long> serviceId) {

        var details = userDetail.getDetails();
        var userId = userDetail.getPrincipal();
        var isOwner = details.getId().equals(userId);

        return recordService.createPendingAtomic(userId, recordId, serviceId)
                .map(it -> mapRecord(it, isOwner, userId));
    }

    // Map<Integer, List<RecordCalendarDto>>
    @GetMapping("/calendar")
    public Mono<Map<Integer, Set<RecordCalendarDto>>> getCalendar(
            UserData userData,
            @RequestParam Long userId,
            @RequestParam int month,
            @RequestParam(required = false, defaultValue = "#{T(java.time.Year).now().value.toString()}") int year) {

        var from = LocalDate.of(year, month, 1);
        var to = YearMonth.of(year, month).atEndOfMonth();

        var data = recordService.getRecordsWithPendingsByUserdId(userId, from, to);
        var requester = userData.getPrincipal();
        var isOwner = userData.getPrincipal().equals(userId);

        return data.map(it -> RecordCalendarDto.builder()
                .ts(it.getTsFrom())
                .canPending(recordService.isRecordPendingable(it, requester))
                .hasPendings(!it.getRecordPendings().isEmpty())
                .day(it.getTsFrom().getDayOfMonth())
                .text(isOwner ? it.getComment() : null)
                .color(it.getColor())
                .isOwner(it.getRecordOwnerId().equals(requester))
                .build())
                .collectList()
                .map(it -> it.stream()
                        .collect(Collectors.groupingBy(e -> e.day(),
                                Collectors.toCollection(() -> new TreeSet<>(
                                        Comparator.comparing(RecordCalendarDto::ts))))));
    }

    private RecordPendingsServiceResponsePreviewDto mapRecord(RecordEntity entity, boolean isOwner, Long userId) {
        var isPendingable = recordService.isRecordPendingable(entity, userId);
        return recordMapper.toDto(entity, isPendingable, isOwner);
    }

}