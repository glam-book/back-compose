package com.tlback.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.tlback.core.model.DomainUserEntity;
import com.tlback.core.model.contact.ContactProvider;
import com.tlback.core.model.contact.GenericContact;
import com.tlback.core.model.contact.Supports;
import com.tlback.core.model.contact.TgContact;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ContactMapper {

    public TgContact map(DomainUserEntity domainUser) {
        return domainUser.getTgUser()
                .map(it -> TgContact.builder()
                        .firstName(it.getFirstName())
                        .lastName(it.getLastName())
                        .tgUserName(it.getUsername())
                        .build())
                .orElse(TgContact.builder().build());
    }

    public ContactProvider of(Supports source, DomainUserEntity entity) {
        return switch (source) {
            case TG -> map(entity);
            default -> new GenericContact(entity.getName()); // TODO fill other, check identity
        };
    }
}
