package com.tlback.domain.service.confirm;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tlback.domain.model.DomainUserEntity;
import com.tlback.domain.model.contact.Supports;
import com.tlback.domain.model.utils.PendingConfirmInfo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@SuppressWarnings({"rawtypes", "unchecked"})
public class ConfirmRequestJob implements Job {
    private Map<Supports, ContactPendingConfirmAction> actions;

    @Override
    public void execute(JobExecutionContext jobCtx) throws JobExecutionException {
        var user = jobCtx.getJobDetail().getJobDataMap().get("user");
        var pending = jobCtx.getJobDetail().getJobDataMap().get("pendingConfirmInfo");
        var actionSupport = jobCtx.getJobDetail().getJobDataMap().get("supports");

        if (
                (user instanceof DomainUserEntity domainUser) &&
                (pending instanceof PendingConfirmInfo pendingConfirmInfo) &&
                (actionSupport instanceof String s)
        ) {

            var action = actions.get(Supports.valueOf(s));
            log.info("Sending pending confirm request to user {}", domainUser.getId());

            domainUser.getTgUser()
                    .peek(tgUser -> action.sendConfirmRequest(tgUser, pendingConfirmInfo))
                    .onEmpty(() -> log.warn("No target contact {} found from domain user", Supports.TG));
        }
    }

    @Autowired
    public void setActions(List<ContactPendingConfirmAction> actions) {
        this.actions = actions.stream()
                .collect(Collectors.toMap(ContactPendingConfirmAction::supports,
                        Function.identity()));
    }

}
