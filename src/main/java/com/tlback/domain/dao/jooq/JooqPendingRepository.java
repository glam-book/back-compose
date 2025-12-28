package com.tlback.domain.dao.jooq;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import com.tlback.domain.dao.jooq.modules.JoinModule;
import com.tlback.domain.model.RecordPending;
import com.tlback.domain.model.ServiceInfoEntity;
import com.tlback.jooq.gen.tables.DomainUser;
import com.tlback.jooq.gen.tables.ServcieInfoToPending;
import com.tlback.jooq.gen.tables.ServiceInfo;
import com.tlback.jooq.gen.tables.records.RecordPendingRecord;
import com.tlback.jooq.gen.tables.records.ServiceInfoRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JooqPendingRepository {
	private static final com.tlback.jooq.gen.tables.RecordPending pendingTable = com.tlback.jooq.gen.tables.RecordPending.RECORD_PENDING;
	private static final com.tlback.jooq.gen.tables.Record recordTable = com.tlback.jooq.gen.tables.Record.RECORD;
	private static final DomainUser userTable = DomainUser.DOMAIN_USER;
	private static final ServcieInfoToPending serviceToPendingTable = ServcieInfoToPending.SERVCIE_INFO_TO_PENDING;
	private static final ServiceInfo serviceInfoTable = ServiceInfo.SERVICE_INFO;

	public static final JoinModule USER_JOIN_MODULE = ctx -> JooqUserRepository.SUB_JOINS.apply(ctx.leftJoin(userTable)
			.on(pendingTable.CLIENT_ID
					.eq(userTable.ID)));

	public static final JoinModule SERVICE_JOIN_MODULE = ctx -> ctx.leftJoin(serviceToPendingTable)
			.on(pendingTable.ID.eq(serviceToPendingTable.PENDING_ID))
			.leftJoin(serviceInfoTable)
			.on(serviceToPendingTable.SERVICE_INFO_ID.eq(serviceInfoTable.ID));

	private final DSLContext dsl;

	public Mono<RecordPending> createPending(Long initiatorId, Long recordId) {
		var query = dsl.insertInto(pendingTable)
				.set(pendingTable.CONFIRMED, false)
				.set(pendingTable.RECORD_ID, recordId)
				.set(pendingTable.CLIENT_ID, initiatorId)
				.set(pendingTable.REQUEST_TIME, LocalDateTime.now())
				.returning();

		return Mono.from(query)
				.map(rec -> rec.into(RecordPending.class));
	}

	public static SelectJoinStep<org.jooq.Record> basicFetch(DSLContext dsl) {
		return dsl.select().from(pendingTable);
	}

	public static SelectJoinStep<org.jooq.Record> fetch(DSLContext dsl, JoinModule... joins) {
		var mainFetch = basicFetch(dsl);

		if (joins != null) {
			for (var join : joins)
				mainFetch = join.apply(mainFetch);
		}

		return mainFetch;
	}

	public Mono<RecordPending> createPendingAtomic(Long initiatorId, Long recordId) {
		var query = dsl.insertInto(pendingTable)
				.columns(pendingTable.RECORD_ID, pendingTable.CLIENT_ID, pendingTable.CONFIRMED,
						pendingTable.REQUEST_TIME)
				.select(DSL.select(DSL.val(recordId), DSL.val(initiatorId), DSL.val(false),
						DSL.val(LocalDateTime.now()))
						.where(DSL.selectCount()
								.from(pendingTable)
								.where(pendingTable.RECORD_ID.eq(recordId))
								.lt(DSL.select(recordTable.RECORD_LIMIT)
										.from(recordTable))))
				.returning();

		log.info(query.toString());
		return Mono.from(query)
				.map(rec -> rec.into(RecordPending.class));
	}

	public Mono<RecordPendingRecord> createPendingAtomic(Long initiatorId, Long recordId, Set<Long> serviceIds) {
		var query = dsl.insertInto(pendingTable)
				.columns(pendingTable.RECORD_ID, pendingTable.CLIENT_ID, pendingTable.CONFIRMED,
						pendingTable.REQUEST_TIME)
				.select(DSL.select(DSL.val(recordId), DSL.val(initiatorId), DSL.val(false),
						DSL.val(LocalDateTime.now()))
						.where(DSL.selectCount()
								.from(pendingTable)
								.where(pendingTable.RECORD_ID.eq(recordId))
								.lt(DSL.select(recordTable.RECORD_LIMIT)
										.from(recordTable)
										.where(recordTable.ID.eq(recordId)))))
				.returning();

		log.info(query.toString());

		return Mono.from(query)
				.map(rec -> rec.into(RecordPendingRecord.class))
				.flatMap(pending -> {
					if (serviceIds == null || serviceIds.isEmpty()) {
						return Mono.just(pending);
					}

					// Создаем batch insert для service связей
					var batchQueries = serviceIds.stream()
							.map(serviceId -> dsl.insertInto(serviceToPendingTable)
									.set(serviceToPendingTable.SERVICE_INFO_ID, serviceId)
									.set(serviceToPendingTable.PENDING_ID, pending.getId()))
							.toList();

					var batchQuery = dsl.batch(batchQueries);

					log.info(batchQuery.toString());
					return Mono.from(batchQuery)
							.thenReturn(pending);
				});
	}

	public Flux<RecordPending> findByRecordId(Long recordId, JoinModule... joinModules) {
		var sql = fetch(dsl, joinModules)
				.where(pendingTable.RECORD_ID.eq(recordId));

		log.info(sql.toString());

		var cache = new HashMap<Long, RecordPending>();
		return Flux.from(sql)
				.map(it -> tryToMap(it, cache));
	}

	public Flux<RecordPending> findByPendingId(Long pendingId, JoinModule... joinModules) {
		var sql = fetch(dsl, joinModules)
			.where(pendingTable.ID.eq(pendingId));

		log.info(sql.toString());

		var cache = new HashMap<Long, RecordPending>();
		return Flux.from(sql)
				.map(it -> tryToMap(it, cache));
	}

	public Flux<ServiceInfoRecord> findServicesByPendingId(Long pendingId) {
		var sql = dsl.select(serviceInfoTable.fields())
			.from(serviceInfoTable)
			.join(serviceToPendingTable)
				.on(serviceToPendingTable.SERVICE_INFO_ID.eq(serviceInfoTable.ID))
			.where(serviceToPendingTable.PENDING_ID.eq(pendingId));

		log.info(sql.toString());
		return Flux.from(sql)
			.map(rec -> rec.into(ServiceInfoRecord.class));
	}

	private RecordPending tryToMap(Record record, Map<Long, RecordPending> mapped) {
		var pendingId = record.get(pendingTable.ID);
		var mainEntity = mapped.computeIfAbsent(pendingId,
				k -> record.into(pendingTable.fields())
						.into(RecordPending.class));

		if (mainEntity != null) {
			var userId = record.get(userTable.ID);
			if (userId != null && mainEntity.getPendingOwner() == null) {
				var domainUser = JooqUserRepository.mapUser(record);
				mainEntity.setPendingOwner(domainUser);
			}

			var serviceId = record.get(serviceInfoTable.ID);
			if (serviceId != null) {
				var service = record.into(serviceInfoTable.fields())
						.into(ServiceInfoEntity.class);
				mainEntity.getServices().add(service);
			}
		}

		return mainEntity;
	}

	public Mono<Boolean> confirmPending(Long recPendingId, boolean isConfiremd) {
		var query = dsl.update(pendingTable)
				.set(pendingTable.CONFIRMED, isConfiremd)
				.where(pendingTable.ID.eq(recPendingId));
		return Mono.from(query).map(it -> it > 0);
	}

}
