package com.tlback.domain.service.confirm;

import com.tlback.domain.model.contact.Supports;
import com.tlback.domain.model.utils.PendingConfirmInfo;

public interface ContactPendingConfirmAction<T> {
    void sendConfirmRequest(T targetContact, PendingConfirmInfo rec);
    Supports supports();
}