package com.tlback.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.tlback.domain.ServiceOwnerInfo;
import com.tlback.web.dto.ServiceOwnerInfoDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceOwnerMapper {

    ServiceOwnerInfoDto toDto(ServiceOwnerInfo entity);
}
