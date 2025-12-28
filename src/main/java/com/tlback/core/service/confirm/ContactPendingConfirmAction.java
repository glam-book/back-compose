package com.tlback.core.service.confirm;

import com.tlback.core.model.contact.Supports;
import com.tlback.core.model.utils.PendingConfirmInfo;

public interface ContactPendingConfirmAction<T> {
    void sendConfirmRequest(T targetContact, PendingConfirmInfo rec);
    Supports supports();
}