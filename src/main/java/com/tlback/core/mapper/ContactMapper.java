package com.tlback.core.mapper;

import java.util.List;
import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.tlback.core.model.DomainUserEntity;
import com.tlback.core.model.contact.ContactProvider;
import com.tlback.core.model.contact.GenericContact;
import com.tlback.core.model.contact.Supports;
import com.tlback.core.model.contact.TgContact;

import io.vavr.control.Option;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ContactMapper {

    public Option<TgContact> mapTgContact(DomainUserEntity domainUser) {
        return domainUser.getTgUser()
                .map(it -> TgContact.builder()
                        .firstName(it.getFirstName())
                        .lastName(it.getLastName())
                        .tgUserName(it.getUsername())
                        .build());
                
    }

    public List<ContactProvider> mapList(DomainUserEntity domainUser) {
        return List.<ContactProvider>of(mapTgContact(domainUser).getOrNull())
            .stream()
            .filter(Objects::nonNull)
            .toList();
    }

    public Option<? extends ContactProvider> of(Supports source, DomainUserEntity entity) {
        return switch (source) {
            case TG -> mapTgContact(entity);
            default -> Option.of(new GenericContact(entity.getName())); // TODO fill other, check identity
        };
    }
}
