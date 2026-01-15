package com.tlback.domain.service.confirm;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tlback.domain.dao.jooq.JooqPendingRepository;
import com.tlback.domain.model.PendingState;
import com.tlback.domain.service.UserService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PendingConfirmationServiceImpl implements PendingConfirmationService {
	private final RecordConfirmScheduler recordConfirmScheduler;
	private final JooqPendingRepository pendingRepository;
	private final UserService userService;

	@Value("${tlback.pending.confirmation.min-activation:3h}")
	private Duration minActivationTime;

	@Override
	public Mono<Void> onPendingCreated(Long pendingId) {
		// TODO later
		// var timeLimit = targetRecord.getTsFrom()
		// 		.minus(minActivationTime);
		// var currentRequestTime = pending.getRequestTime();
		// var isBefore = currentRequestTime.isBefore(timeLimit.toLocalDateTime());
		// return isBefore ? initStream.flatMap(
		// 		it -> recordConfirmScheduler.scheduleConfirmation(pending, targetRecord, timeLimit))
		// 		: initStream.and(pendingRepository.confirmPending(pending.getId(), PendingState.CONFIRMED));
		return Mono.empty();
	}

	@Override
	public Mono<Void> onPendingCancelled(Long pendingId) {
		return pendingRepository.confirmPending(pendingId, PendingState.CANCELLED)
				.then();
	}

	// TODO remove pending scheduler
	@Override
	public Mono<Void> onPendingConfirmed(Long pendingId) {
		return pendingRepository
				.confirmPending(pendingId, PendingState.CONFIRMED)
				.then();
	}

}