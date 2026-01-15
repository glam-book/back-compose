package com.tlback.domain.service;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.jooq.exception.IntegrityConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.app.dto.records.OptionalRecordCreateOrUpdateRequest;
import com.tlback.domain.abac.AbacService;
import com.tlback.domain.abac.exception.NotFoundException;
import com.tlback.domain.dao.jooq.JooqPendingRepository;
import com.tlback.domain.dao.jooq.JooqRecordRepository;
import com.tlback.domain.dao.jooq.JooqRecordToServiceRepository;
import com.tlback.domain.model.DomainUserEntity;
import com.tlback.domain.model.RecordEntity;
import com.tlback.domain.model.RecordPending;
import com.tlback.domain.model.ServiceInfoEntity;
import com.tlback.domain.model.utils.RecordFilter;
import com.tlback.domain.notifier.api.NotificationRequest;
import com.tlback.domain.notifier.api.UserNotifier;
import com.tlback.domain.service.confirm.PendingConfirmationService;
import com.tlback.domain.service.exception.RecordPendingException;
import com.tlback.domain.tools.ZoneOffsetTools;
import com.tlback.jooq.gen.tables.records.RecordRecord;
import com.tlback.jooq.gen.tables.records.ServiceInfoRecord;

import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {

	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy 'в' HH:mm")
			.withLocale(Locale.of("ru"));

	private final UserNotifier<DomainUserEntity> userNotifier;
	private final JooqRecordRepository recordRepository;
	private final ServiceInfoService serviceInfoService;
	private final JooqPendingRepository pendingRepository;
	private final JooqRecordToServiceRepository recordToServiceRepository;
	private final PendingConfirmationService pendingConfirmationService;
	private final UserService userService;
	private final AbacService abac;

	public boolean isRecordPendingable(RecordEntity entity, Long userId) {
		var isOwner = userId.equals(entity.getRecordOwnerId());
		var isFitByPendings = Optional.ofNullable(entity.getRecordPendings())
				.map(pendingds -> {
					var hasMyPendings = pendingds.stream()
							.anyMatch(pending -> pending.getClientId() != null && pending.getClientId().equals(userId));
					return !hasMyPendings && (pendingds.size() < entity.getRecordLimit());
				}).orElse(false);

		return !isOwner && isFitByPendings;
	}

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

		return recordRepository.findFullByFilter(filter)
				.filter(it -> {
					var isOwner = it.getRecordOwnerId().equals(userId);
					var rights = abac.parse(it.getRecordPermissions());
					return isOwner ? rights.canOwnerRead() : rights.canOtherRead();
				});
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
	public Flux<RecordEntity> getRecordsWithPendingsByUserdId(Long userId, LocalDate from, LocalDate to) {
		var filter = RecordFilter.builder()
				.recordOwnerId(userId)
				.dateFrom(from.atStartOfDay())
				.dateTo(to.atStartOfDay()).build();

		return recordRepository.findByFilter(filter,
				List.of(JooqRecordRepository.JOIN_RECORD_PENDINGS));
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

	// TODO remove update request dto - use model
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
											() -> recordRepository.update(mapToRecord(cmd, userId)),
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

	@Transactional
	public Flux<RecordPending> getPendingDetails(Long recordId, Long requester) {
		Flux<RecordPending> fetchPendings = pendingRepository.findByRecordId(recordId,
				JooqPendingRepository.USER_JOIN_MODULE,
				JooqPendingRepository.SERVICE_JOIN_MODULE);
		return abac.fetchRecordRights(recordId, requester)
				.flatMapMany(rightsCtx -> rightsCtx.onReadAllowMap(() -> fetchPendings,
						Flux::error));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public Mono<RecordEntity> createPendingAtomic(Long initiatorId, Long targetRecordId,
			Set<Long> targetServices) {
		return pendingRepository.createPendingAtomic(
				initiatorId,
				targetRecordId,
				targetServices, JooqPendingRepository.SERVICE_JOIN_MODULE)
				.switchIfEmpty(Mono.error(new RecordPendingException("Limit reached")))
				.onErrorMap(ex -> ex instanceof IntegrityConstraintViolationException,
						ex -> new RecordPendingException("Pending already exists", ex))
				.onErrorMap(ex -> ex instanceof R2dbcDataIntegrityViolationException,
						ex -> new RecordPendingException("Cannot create pending", ex))
				.zipWhen(it -> getRecordsWithPendingsAndServiceById(targetRecordId))
				.flatMap(tuple -> {
					var rec = tuple.getT2();
					var recPending = tuple.getT1();
					var recordOwner = rec.getRecordOwnerId();
					return userService.findById(recordOwner)
							.flatMap(user -> pendingConfirmationService
									.onPendingCreated(recPending.getId())
									.thenReturn(user))
							.flatMap(user -> sendPendingCreationNotificaion(rec, user))
							.thenReturn(tuple.getT2());
				});
	}

	private Mono<Void> sendPendingCreationNotificaion(
			RecordEntity rec,
			DomainUserEntity recOwner) {
		var notificationRequest = NotificationRequest
				.builder()
				.message(String.format("""
						🎉 Новая заявка на запись!
						⏰ Время начала: %s
						Cервисы: %s
						""",
						rec.getTsFrom().format(DATE_TIME_FORMAT),
						rec.getServiceInfo().stream()
								.map(s -> s.getServiceName() + " : " + s.getPrice().setScale(2, RoundingMode.HALF_UP)
										+ " руб. " +
										(Boolean.TRUE.equals(s.getIsHourlyPrice()) ? "за час" : ""))
								.reduce("", (a, b) -> a + "\n" + b)))
				.build();
		return Mono.fromRunnable(() -> userNotifier.sendNotification(recOwner, notificationRequest));
	}

	public Flux<RecordPending> getRecordPendings(Long recordId) {
		return pendingRepository.findByRecordId(recordId);
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
