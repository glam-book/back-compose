package com.tlback.domain.service.confirm;

import com.tlback.domain.model.contact.UserContactType;
import com.tlback.domain.model.utils.PendingConfirmInfo;

public interface ContactPendingConfirmAction<T> {
    void sendConfirmRequest(T targetContact, PendingConfirmInfo rec);
    UserContactType supports();
}