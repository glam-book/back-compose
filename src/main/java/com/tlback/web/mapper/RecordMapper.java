package com.tlback.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.tlback.domain.RecordEntity;
import com.tlback.web.dto.ServiceOwnerRecordDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {RecordPendingMapper.class})
public interface RecordMapper {

    ServiceOwnerRecordDto toDto(RecordEntity entity);
}
