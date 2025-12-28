package com.tlback.domain.mapper;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.tlback.app.dto.Permissions;
import com.tlback.app.dto.service.OptionalServiceInfoDto;
import com.tlback.app.dto.service.preview.ServicePreviewDto;
import com.tlback.domain.abac.PermissionMask;
import com.tlback.domain.model.ServiceInfoEntity;
import com.tlback.jooq.gen.tables.records.ServiceInfoRecord;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceInfoMapper {

    @Named("map")
    @Mapping(target = "title", source = "serviceName")
    ServicePreviewDto map(ServiceInfoEntity value);

    @Mapping(target = "title", source = "serviceName")
    @Mapping(target = "description", source = "serviceDescription")
    @Mapping(target = "id", source = "id", qualifiedByName = "toOptionalLong")
    OptionalServiceInfoDto toDto(ServiceInfoEntity value);

    @Named("toOptionalLong")
    default Optional<Long> toOptionalLong(Long value) {
        return Optional.ofNullable(value);
    }

    @Mapping(target = "title", source = "serviceName")
    @Mapping(target = "description", source = "serviceDescription")
    @Mapping(target = "id", source = "id", qualifiedByName = "toOptionalLong")
    OptionalServiceInfoDto toDto(ServiceInfoRecord it);

    default Permissions map(byte[] mask) {
        return new Permissions(new PermissionMask.Rights(mask));
    }
}
