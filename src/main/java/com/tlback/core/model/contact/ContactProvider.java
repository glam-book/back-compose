package com.tlback.core.model.contact;

public sealed interface ContactProvider permits TgContact, GenericContact, PhoneContact {
    Supports support();
}
