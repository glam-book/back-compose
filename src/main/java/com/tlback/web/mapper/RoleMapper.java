package com.tlback.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.tlback.jpa.entities.Role;
import com.tlback.web.dto.RoleDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper {
    RoleDto toDto(Role role);
}
