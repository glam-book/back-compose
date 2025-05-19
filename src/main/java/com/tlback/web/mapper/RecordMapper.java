package com.tlback.web.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.tlback.domain.RecordEntity;
import com.tlback.web.dto.records.preview.RecordPendingPreviewDto;
import com.tlback.web.dto.records.preview.RecordPendingsServiceResponsePreviewDto;

import io.micrometer.common.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = { RecordPendingMapper.class,
        ServiceInfoMapper.class })
public abstract class RecordMapper {

    @Mapping(target = "tsFrom", qualifiedByName = "toLocalDateTime")
    @Mapping(target = "tsTo", qualifiedByName = "toLocalDateTime")
    @Mapping(target = "recordPendings", source = ".", qualifiedByName = "pendingsToPreview")
    @Mapping(target = "serviceInfo", qualifiedByName = "map")
    public abstract RecordPendingsServiceResponsePreviewDto toDto(RecordEntity entity, @Context ZoneOffset offset);

    @Named("toLocalDateTime")
    public LocalDateTime toLocalDateTime(OffsetDateTime timestamp, @Nullable @Context ZoneOffset offset) {
        if (offset == null)
            return timestamp.toLocalDateTime();
        return timestamp.withOffsetSameInstant(offset).toLocalDateTime();
    }

    @Named("pendingsToPreview")
    public RecordPendingPreviewDto pendingsToPreview(RecordEntity recordEntity) {
        return new RecordPendingPreviewDto(recordEntity.getServiceInfo().getRecordLimit(),
                recordEntity.getRecordPendings().size());
    }

    public String zoneToStr(ZoneOffset tz) {
        return tz.toString();
    }

    public RecordPendingsServiceResponsePreviewDto toDto(RecordEntity entity) {
        return toDto(entity, null);
    }

}
