package com.tlback.core.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.tlback.core.model.DomainUserEntity;
import com.tlback.core.model.contact.ContactProvider;
import com.tlback.web.dto.user.UserDto;
import com.tlback.web.dto.user.UserProfileDto;
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class UserMapper {

    @Autowired
    private ContactMapper contactMapper;

    public abstract UserDto toDto(DomainUserEntity entity);

    @Mapping(target = "contacts", source = ".", qualifiedByName = "mapContacts")
    public abstract UserProfileDto toProfileDto(DomainUserEntity entity);

    @Named("mapContacts")
    public List<ContactProvider> mapContacts(DomainUserEntity entity) {
        return contactMapper.mapList(entity);
    }
}
