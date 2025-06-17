package com.tlback.core.web.mapper;

import java.util.SortedSet;
import java.util.TreeSet;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.tlback.core.model.RecordPending;
import com.tlback.core.web.dto.records.RecordPendingDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class RecordPendingMapper {

    @Mapping(target = "requesterLogin", source = "pendingOwner.login")
    @Mapping(target = "requesterId", source = "pendingOwner.id")
    public abstract RecordPendingDto toDto(RecordPending recordPending);

    public SortedSet<RecordPendingDto> toDto(SortedSet<RecordPending> recordPendings) {
        return recordPendings.stream().map(this::toDto).collect(() -> new TreeSet<>(), SortedSet::add,
                SortedSet::addAll);
    }
}
