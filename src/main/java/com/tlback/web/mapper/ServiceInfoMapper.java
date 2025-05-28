package com.tlback.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.tlback.model.ServiceInfoEntity;
import com.tlback.web.dto.service.ServiceInfoDto;
import com.tlback.web.dto.service.preview.ServicePreviewDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceInfoMapper {

    @Named("map")
    @Mapping(target = "title", source = "serviceName")
    ServicePreviewDto map(ServiceInfoEntity value);

    @Named("toEntity")
    @Mapping(target = "serviceOwnerId", ignore = true)
    @Mapping(target = "serviceName", source = "title")
    @Mapping(target = "editable", ignore = true, defaultValue = "false")
    @Mapping(target = "serviceDescription", source = "description")
    @Mapping(target = "timeDuration", ignore = true)
    @Mapping(target = "serviceOwner", ignore = true)
    @Mapping(target = "records", ignore = true)
    ServiceInfoEntity toEntity(ServiceInfoDto dto);
}
