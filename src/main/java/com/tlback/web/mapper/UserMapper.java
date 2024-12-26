package com.tlback.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.tlback.domain.DomainUserEntity;
import com.tlback.web.dto.UserDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = RoleMapper.class)
public interface UserMapper {

    UserDto toDto(DomainUserEntity user);

    DomainUserEntity toEntity(UserDto userDto);
}
