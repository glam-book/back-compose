package com.tlback.core.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.core.abac.AbacService;
import com.tlback.core.abac.exception.NotFoundException;
import com.tlback.core.common.daofilter.RecordFilter;
import com.tlback.core.dao.jooq.JooqPendingRepository;
import com.tlback.core.dao.jooq.JooqRecordRepository;
import com.tlback.core.model.RecordEntity;
import com.tlback.core.service.exception.RecordPendingException;
import com.tlback.core.tools.ZoneOffsetTools;
import com.tlback.core.web.dto.records.OptionalRecordCreateOrUpdateRequest;
import com.tlback.events.core.EventPublisher;
import com.tlback.events.impl.pending.RecordPendingCreatedEvent;
import com.tlback.events.impl.record.RecordCreatedEvent;
import com.tlback.jooq.gen.tables.records.RecordRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {
	private final JooqRecordRepository recordRepository;
	private final ServiceInfoService serviceInfoService;
	private final JooqPendingRepository pendingRepository;
	private final EventPublisher eventPublisher;
	private final AbacService abac;

	@Transactional(readOnly = true)
	public Flux<RecordEntity> getRecords(RecordFilter filter) {
		return recordRepository.findFullByFilter(filter);
	}

	@Transactional(readOnly = true)
	public Flux<RecordEntity> getRecordsByUserId(Long userId, LocalDateTime timeFrom, LocalDateTime timeTo) {
		var filter = RecordFilter.builder()
				.recordOwnerId(userId)
				.dateFrom(timeFrom)
				.dateTo(timeTo).build();
		return recordRepository.findFullByFilter(filter);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	// @Cacheable(value = CacheConfig.RECORD_CACHE_NAME, key = "{#userId, #date}")
	public Flux<RecordEntity> getRecordsWithPendingsAndServiceByUserdId(Long userId, LocalDate date) {
		var fromDate = date.atStartOfDay();
		var toDate = date.plusDays(1).atStartOfDay();

		var filter = RecordFilter.builder()
				.recordOwnerId(userId)
				.dateFrom(fromDate)
				.dateTo(toDate).build();

		return recordRepository.findByFilter(filter,
				List.of(JooqRecordRepository.JOIN_SERVICE_INFO, JooqRecordRepository.JOIN_RECORD_PENDINGS));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public Mono<RecordEntity> getRecordsWithPendingsAndServiceById(Long recordId) {
		var filter = RecordFilter.builder()
				.recordId(recordId)
				.build();

		return recordRepository.findByFilter(
				filter,
				List.of(JooqRecordRepository.JOIN_SERVICE_INFO,
						JooqRecordRepository.JOIN_RECORD_PENDINGS))
				.singleOrEmpty()
				.switchIfEmpty(Mono.error(new NotFoundException("Record not found: " + recordId)));
	}

	@Transactional
	public Mono<RecordEntity> saveOrUpdate(OptionalRecordCreateOrUpdateRequest cmd, Long userId) {
		var joinService = JooqRecordRepository.JOIN_SERVICE_INFO;
		var serviceRequest = cmd.getServiceInfo();
		var service = serviceInfoService.saveOrUpdate(serviceRequest, userId);

		return service.flatMap(serviceMono -> cmd.getId()
				// if record id exists - check access and update record if allowed
				.map(recId -> abac.canModifyRecord(userId, recId)
						// TODO block hooligan user
						.flatMap(abacResult -> abacResult.mapResult(
								() -> recordRepository.update(mapToRecord(cmd, serviceMono.getId(), userId),
										joinService, JooqRecordRepository.JOIN_RECORD_PENDINGS),
								Mono::error)))
				// or else create new one
				.orElseGet(() -> recordRepository.save(mapToRecord(cmd, serviceMono.getId(), userId),
						joinService, JooqRecordRepository.JOIN_RECORD_PENDINGS)))
				.doOnSuccess(it -> {
					eventPublisher
							.publish(new RecordCreatedEvent(this, userId, it.getId(), it.getTsFrom(), it.getTsTo()));
				});
	}

	@Transactional
	public Mono<Boolean> deleteCascadeWithPendings(Long id, Long owner) {
		return abac.canModifyRecord(owner, id)
				.flatMap(abacResult -> recordRepository.delete(id));
	}

	@Transactional
	public Mono<RecordEntity> createPending(Long initiatorId, Long targetRecordId) {
		return recordRepository.findById(targetRecordId,
				JooqRecordRepository.JOIN_SERVICE_INFO,
				JooqRecordRepository.JOIN_RECORD_PENDINGS,
				JooqRecordRepository.JOIN_RECORD_PENDING_USER_INFO)
				.flatMap(record -> {
					var pendings = record.getRecordPendings();
					if (pendings.stream().count() > record.getServiceInfo().getRecordLimit()) {
						return Mono.error(new RecordPendingException("Reached pending limit for this service"));
					} else if (pendings.stream().anyMatch(p -> p.getPendingOwner().getId().equals(initiatorId)))
						return Mono.error(new RecordPendingException("Pending already exists"));
					else
						return Mono.just(record);
				})
				.flatMap(record -> {
					return pendingRepository.createPending(initiatorId, targetRecordId)
						.flatMap(it -> getRecordsWithPendingsAndServiceById(targetRecordId));
				}).doOnSuccess(it ->
					eventPublisher.publish(new RecordPendingCreatedEvent(this, it.getRecordOwnerId(), it.getId()))
				);
	}

	private RecordRecord mapToRecord(OptionalRecordCreateOrUpdateRequest cmd, Long serviceId, Long userId) {
		var newRecord = new RecordRecord();
		cmd.getId().ifPresent(newRecord::setId);
		newRecord.setTz(ZoneOffsetTools.DEFAULT_OFFSET); // TODO get from client in future
		newRecord.setIsPublic(true);
		newRecord.setTsFrom(cmd.getTsFrom());
		newRecord.setTsTo(cmd.getTsTo());
		newRecord.setComment(cmd.getComment());
		newRecord.setServiceInfoId(serviceId);
		newRecord.setRecordOwnerId(userId);
		return newRecord;
	}
}
