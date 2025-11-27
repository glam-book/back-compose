package com.tlback.core.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.tlback.core.model.DomainUserEntity;
import com.tlback.core.model.contact.ContactProvider;
import com.tlback.web.dto.user.UserDto;
import com.tlback.web.dto.user.UserProfileDto;

import lombok.RequiredArgsConstructor;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@RequiredArgsConstructor
public abstract class UserMapper {
    private final ContactMapper contactMapper;

    public abstract UserDto toDto(DomainUserEntity entity);

    @Mapping(target = "contacts", source = ".", qualifiedByName = "mapContacts")
    public abstract UserProfileDto toProfileDto(DomainUserEntity entity);

    @Named("mapContacts")
    public List<ContactProvider> mapContacts(DomainUserEntity entity) {
        return contactMapper.mapList(entity);
    }
}
