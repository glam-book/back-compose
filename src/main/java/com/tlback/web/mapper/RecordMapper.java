package com.tlback.web.mapper;

import com.tlback.domain.Record;
import com.tlback.web.dto.RecordDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = ServiceOwnerMapper.class)
public interface RecordMapper {

    RecordDto toDto(Record entity);
}
