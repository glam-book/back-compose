package com.tlback.core.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.tlback.core.abac.PermissionMask;
import com.tlback.core.model.RecordEntity;
import com.tlback.web.dto.Permissions;
import com.tlback.web.dto.records.preview.RecordPendingPreviewDto;
import com.tlback.web.dto.records.preview.RecordPendingsServiceResponsePreviewDto;

import io.micrometer.common.lang.Nullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = { RecordPendingMapper.class,
        ServiceInfoMapper.class })
public abstract class RecordMapper {

    @Mapping(target = "tsFrom", qualifiedByName = "toLocalDateTime")
    @Mapping(target = "tsTo", qualifiedByName = "toLocalDateTime")
    @Mapping(target = "recordPendings", source = "entity", qualifiedByName = "pendingsToPreview")
    @Mapping(target = "serviceInfo", qualifiedByName = "map")
    @Mapping(target = "comment", expression = "java(owner ? entity.getComment() : null)")
    public abstract RecordPendingsServiceResponsePreviewDto toDto(RecordEntity entity,
            @Context ZoneOffset offset,
            boolean pendigable, 
            boolean owner);

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
        return new RecordPendingPreviewDto(recordEntity.getRecordLimit(),
                recordEntity.getRecordPendings().size());
    }

    public String zoneToStr(ZoneOffset tz) {
        return tz.toString();
    }

    @Mapping(target = "tsFrom", qualifiedByName = "toLocalDateTime")
    public RecordPendingsServiceResponsePreviewDto toDto(RecordEntity entity, 
            boolean pendigable, 
            boolean owner) {
        return toDto(entity, null, pendigable, owner);
    }

    public Permissions map(byte[] mask) {
        return new Permissions(new PermissionMask.Rights(mask));
    }

}
