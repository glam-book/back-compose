package com.tlback.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.tlback.core.model.DomainUserEntity;
import com.tlback.web.dto.user.UserDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    
    UserDto toDto(DomainUserEntity entity);
}
