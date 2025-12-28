package com.tlback.domain.model.contact;

public sealed interface ContactProvider permits TgContact, GenericContact, PhoneContact {
    Supports support();
}
