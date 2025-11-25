package com.tlback.core.dao.jooq;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import com.tlback.core.dao.jooq.modules.JoinModule;
import com.tlback.core.model.RecordPending;
import com.tlback.core.model.ServiceInfoEntity;
import com.tlback.jooq.gen.tables.DomainUser;
import com.tlback.jooq.gen.tables.ServcieInfoToPending;
import com.tlback.jooq.gen.tables.ServiceInfo;

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

	public static final JoinModule USER_JOIN_MODULE = dsl -> JooqUserRepository.SUB_JOINS.apply(dsl.join(userTable)
			.on(pendingTable.CLIENT_ID
					.eq(userTable.ID)));

	public static final JoinModule SERVICE_JOIN_MODULE = dsl -> dsl.join(serviceToPendingTable)
			.on(pendingTable.ID.eq(serviceToPendingTable.PENDING_ID))
			.join(serviceInfoTable)
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

	public Mono<RecordPending> createPendingAtomic(Long initiatorId, Long recordId, Set<Long> serviceIds) {
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
				.map(rec -> rec.into(RecordPending.class))
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
}
