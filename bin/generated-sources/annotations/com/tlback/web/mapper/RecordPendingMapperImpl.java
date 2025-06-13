package com.tlback.web.mapper;

import com.tlback.model.DomainUserEntity;
import com.tlback.model.RecordPending;
import com.tlback.web.dto.records.RecordPendingDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-13T05:55:29+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.42.50.v20250610-1544, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class RecordPendingMapperImpl extends RecordPendingMapper {

    @Override
    public RecordPendingDto toDto(RecordPending recordPending) {
        if ( recordPending == null ) {
            return null;
        }

        RecordPendingDto recordPendingDto = new RecordPendingDto();

        recordPendingDto.setRequesterLogin( recordPendingPendingOwnerLogin( recordPending ) );
        recordPendingDto.setRequesterId( recordPendingPendingOwnerId( recordPending ) );
        recordPendingDto.setConfirmed( recordPending.getConfirmed() );
        recordPendingDto.setRequestTime( recordPending.getRequestTime() );

        return recordPendingDto;
    }

    private String recordPendingPendingOwnerLogin(RecordPending recordPending) {
        DomainUserEntity pendingOwner = recordPending.getPendingOwner();
        if ( pendingOwner == null ) {
            return null;
        }
        return pendingOwner.getLogin();
    }

    private Long recordPendingPendingOwnerId(RecordPending recordPending) {
        DomainUserEntity pendingOwner = recordPending.getPendingOwner();
        if ( pendingOwner == null ) {
            return null;
        }
        return pendingOwner.getId();
    }
}
