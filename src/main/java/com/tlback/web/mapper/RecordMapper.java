package com.tlback.web.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.lang.Nullable;

import com.tlback.domain.RecordEntity;
import com.tlback.web.dto.ServiceOwnerRecordDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {RecordPendingMapper.class})
public interface RecordMapper {

    @Mapping(target = "timeFrom", qualifiedByName = "toLocalDateTime")
    @Mapping(target = "timeTo", qualifiedByName = "toLocalDateTime")
    @Mapping(target = "originalTz", source = "tz")
    ServiceOwnerRecordDto toDto(RecordEntity entity, @Context ZoneOffset offset);

    @Named("toLocalDateTime")
    default LocalDateTime toLocalDateTime(OffsetDateTime timestamp, @Nullable @Context ZoneOffset offset) {
        if (offset == null)
            return timestamp.toLocalDateTime();
        return timestamp.withOffsetSameInstant(offset).toLocalDateTime();
    }

    default String zoneToStr(ZoneOffset tz) {
        return tz.toString();
    }

    default ServiceOwnerRecordDto toDto(RecordEntity entity) {
        return toDto(entity, null);
    }

}
