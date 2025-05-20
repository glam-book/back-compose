package com.tlback.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.tlback.domain.ServiceInfoEntity;
import com.tlback.web.dto.records.preview.ServicePreviewDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceInfoMapper {

    @Named("map")
    @Mapping(target = "title", source = "serviceName")
    ServicePreviewDto map(ServiceInfoEntity value);
}
