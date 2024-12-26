package com.tlback.web.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.tlback.domain.RecordEntity;
import com.tlback.web.dto.ServiceOwnerRecordDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {RecordPendingMapper.class})
public interface RecordMapper {

    ServiceOwnerRecordDto toDto(RecordEntity entity);

    default LocalDateTime toLocalDateTime(OffsetDateTime timestamp) {
        return timestamp.toLocalDateTime();
    }
}
