package com.tlback.web.mapper;

import com.tlback.model.RecordEntity;
import com.tlback.model.ServiceInfoEntity;
import com.tlback.web.dto.records.RecordPreviewResponse;
import com.tlback.web.dto.records.preview.RecordPendingsServiceResponsePreviewDto;
import java.time.ZoneOffset;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-13T05:55:29+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.42.50.v20250610-1544, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class RecordMapperImpl extends RecordMapper {

    @Autowired
    private ServiceInfoMapper serviceInfoMapper;

    @Override
    public RecordPendingsServiceResponsePreviewDto toDto(RecordEntity entity, ZoneOffset offset) {
        if ( entity == null ) {
            return null;
        }

        RecordPendingsServiceResponsePreviewDto recordPendingsServiceResponsePreviewDto = new RecordPendingsServiceResponsePreviewDto();

        recordPendingsServiceResponsePreviewDto.setTsFrom( map( entity.getTsFrom(), offset ) );
        recordPendingsServiceResponsePreviewDto.setTsTo( map( entity.getTsTo(), offset ) );
        recordPendingsServiceResponsePreviewDto.setRecordPendings( pendingsToPreview( entity ) );
        recordPendingsServiceResponsePreviewDto.setServiceInfo( serviceInfoMapper.map( entity.getServiceInfo() ) );
        recordPendingsServiceResponsePreviewDto.setId( entity.getId() );

        return recordPendingsServiceResponsePreviewDto;
    }

    @Override
    public RecordPreviewResponse toPreviewResponse(RecordEntity entity) {
        if ( entity == null ) {
            return null;
        }

        RecordPreviewResponse recordPreviewResponse = new RecordPreviewResponse();

        recordPreviewResponse.setServiceInfoId( entityServiceInfoId( entity ) );
        recordPreviewResponse.setId( entity.getId() );
        recordPreviewResponse.setTsFrom( map( entity.getTsFrom() ) );
        recordPreviewResponse.setTsTo( map( entity.getTsTo() ) );

        return recordPreviewResponse;
    }

    private Long entityServiceInfoId(RecordEntity recordEntity) {
        ServiceInfoEntity serviceInfo = recordEntity.getServiceInfo();
        if ( serviceInfo == null ) {
            return null;
        }
        return serviceInfo.getId();
    }
}
