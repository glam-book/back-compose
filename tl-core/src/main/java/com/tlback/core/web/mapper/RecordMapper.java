package com.tlback.core.web.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.tlback.core.model.RecordEntity;
import com.tlback.core.web.dto.records.RecordPreviewResponse;
import com.tlback.core.web.dto.records.preview.RecordPendingPreviewDto;
import com.tlback.core.web.dto.records.preview.RecordPendingsServiceResponsePreviewDto;

import io.micrometer.common.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = { RecordPendingMapper.class,
        ServiceInfoMapper.class })
public abstract class RecordMapper {

    @Mapping(target = "tsFrom", qualifiedByName = "toLocalDateTime")
    @Mapping(target = "tsTo", qualifiedByName = "toLocalDateTime")
    @Mapping(target = "recordPendings", source = "entity", qualifiedByName = "pendingsToPreview")
    @Mapping(target = "serviceInfo", qualifiedByName = "map")
    public abstract RecordPendingsServiceResponsePreviewDto toDto(RecordEntity entity, @Context ZoneOffset offset);

    @Named("toLocalDateTime")
    public LocalDateTime map(OffsetDateTime timestamp, @Nullable @Context ZoneOffset offset) {
        if (offset == null)
            return timestamp.toLocalDateTime();
        return timestamp.withOffsetSameInstant(offset).toLocalDateTime();
    }

    public LocalDateTime map(OffsetDateTime timestamp) {
        return this.map(timestamp, null);
    }

    @Named("toOffsetDateTime")
    public OffsetDateTime map(LocalDateTime timestamp, @Context ZoneOffset offset) {
        return timestamp.atOffset(offset);
    }

    @Named("pendingsToPreview")
    public RecordPendingPreviewDto pendingsToPreview(RecordEntity recordEntity) {
        return new RecordPendingPreviewDto(recordEntity.getServiceInfo().getRecordLimit(),
                recordEntity.getRecordPendings().size());
    }

    public String zoneToStr(ZoneOffset tz) {
        return tz.toString();
    }

    @Mapping(target = "tsFrom", qualifiedByName = "toLocalDateTime")
    public RecordPendingsServiceResponsePreviewDto toDto(RecordEntity entity) {
        return toDto(entity, null);
    }

    @Mapping(target = "serviceInfoId", source = "serviceInfo.id")
    public abstract RecordPreviewResponse toPreviewResponse(RecordEntity entity);

}
