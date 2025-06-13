package com.tlback.web.mapper;

import com.tlback.model.ServiceInfoEntity;
import com.tlback.web.dto.service.preview.ServicePreviewDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-13T05:55:29+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.42.50.v20250610-1544, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@Component
public class ServiceInfoMapperImpl implements ServiceInfoMapper {

    @Override
    public ServicePreviewDto map(ServiceInfoEntity value) {
        if ( value == null ) {
            return null;
        }

        ServicePreviewDto servicePreviewDto = new ServicePreviewDto();

        servicePreviewDto.setTitle( value.getServiceName() );
        servicePreviewDto.setId( value.getId() );

        return servicePreviewDto;
    }
}
