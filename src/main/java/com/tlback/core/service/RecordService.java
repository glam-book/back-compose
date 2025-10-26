package com.tlback.core.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.jooq.exception.IntegrityConstraintViolationException;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.core.abac.AbacService;
import com.tlback.core.abac.exception.NotFoundException;
import com.tlback.core.common.daofilter.RecordFilter;
import com.tlback.core.dao.jooq.JooqPendingRepository;
import com.tlback.core.dao.jooq.JooqRecordRepository;
import com.tlback.core.dao.jooq.JooqRecordToServiceRepository;
import com.tlback.core.model.RecordEntity;
import com.tlback.core.model.RecordPending;
import com.tlback.core.service.exception.RecordPendingException;
import com.tlback.core.tools.ZoneOffsetTools;
import com.tlback.events.core.EventPublisher;
import com.tlback.events.impl.pending.RecordPendingCreatedEvent;
import com.tlback.events.impl.record.RecordUpdatedEvent;
import com.tlback.jooq.gen.tables.records.RecordRecord;
import com.tlback.jooq.gen.tables.records.ServiceInfoRecord;
import com.tlback.web.dto.records.OptionalRecordCreateOrUpdateRequest;

import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {
	private final JooqRecordRepository recordRepository;
	private final ServiceInfoService serviceInfoService;
	private final JooqPendingRepository pendingRepository;
	private final JooqRecordToServiceRepository recordToServiceRepository;
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

	public Mono<RecordEntity> saveOrUpdate(OptionalRecordCreateOrUpdateRequest cmd, Long userId) {
		var joinService = JooqRecordRepository.JOIN_SERVICE_INFO;
		var serviceRequest = cmd.getServiceInfo();

		return serviceInfoService.saveOrUpdate(serviceRequest, userId)
				.collectList()
				.flatMap(list -> {
					var recordMono = cmd.getId()
							// if record id exists - check access and update record if allowed
							.map(recId -> abac.canModifyRecord(userId, recId)
									.flatMap(abacResult -> abacResult.mapResult(
											() -> recordRepository.update(mapToRecord(cmd, userId))
													.doOnSuccess(it -> {
														eventPublisher.publish(RecordUpdatedEvent.builder()
																.source(this)
																.recId(recId)
																.end(it.getTsTo())
																.start(it.getTsFrom())
																.userId(userId)
																.build());
													}),
											Mono::error)))
							// or else create new one
							.orElseGet(() -> recordRepository.save(
									mapToRecord(cmd, userId)));

					// После того как рекорд создан/обновлён, линкуем сервисы
					return recordMono.flatMap(record -> recordToServiceRepository
							.linkRecordToService(record.getId(), list.stream()
									.map(ServiceInfoRecord::getId)
									.toList())
							.then(recordRepository.findById(record.getId(),
									joinService,
									JooqRecordRepository.JOIN_RECORD_PENDINGS)));
				});
	}

	@Transactional
	public Mono<Boolean> deleteCascadeWithPendings(Long id, Long owner) {
		return abac.canModifyRecord(owner, id)
				.flatMap(abacResult -> recordRepository.delete(id));
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Mono<RecordEntity> createPendingAtomic(Long initiatorId, Long targetRecordId, List<Long> targetServices) {
		return pendingRepository.createPendingAtomic(initiatorId, targetRecordId, targetServices)
				.switchIfEmpty(Mono.error(new RecordPendingException("Limit reached")))
				.onErrorMap(
						ex -> ex instanceof IntegrityConstraintViolationException
								|| ex instanceof R2dbcDataIntegrityViolationException,
						ex -> new RecordPendingException("Cannot create pending", ex))
				.flatMap(it -> getRecordsWithPendingsAndServiceById(targetRecordId))
				.doOnSuccess(it -> eventPublisher
						.publish(new RecordPendingCreatedEvent(this, it.getRecordOwnerId(), it.getId())))
				.retryWhen(Retry.max(3)
						.filter(ex -> ex instanceof SerializationFailedException));
	}

	public Flux<RecordPending> getRecordPendings(Long recordId) {
		return pendingRepository.findById(recordId);
	}

	private RecordRecord mapToRecord(OptionalRecordCreateOrUpdateRequest cmd, Long userId) {
		var newRecord = new RecordRecord();
		cmd.getId().ifPresent(newRecord::setId);
		newRecord.setTz(ZoneOffsetTools.DEFAULT_OFFSET); // TODO get from client in future
		newRecord.setIsPublic(true);
		newRecord.setTsFrom(cmd.getTsFrom());
		newRecord.setTsTo(cmd.getTsTo());
		newRecord.setComment(cmd.getComment());
		newRecord.setRecordOwnerId(userId);
		return newRecord;
	}
}
