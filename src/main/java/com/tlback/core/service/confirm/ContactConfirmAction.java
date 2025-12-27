package com.tlback.core.service.confirm;

import com.tlback.core.model.contact.Supports;

public interface ContactConfirmAction<T> {
    void sendConfirmRequest(T targetContact, Long recPendingId);
    Supports supports();
}