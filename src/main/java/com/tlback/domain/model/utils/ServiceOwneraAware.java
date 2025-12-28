package com.tlback.domain.model.utils;

import com.tlback.domain.model.DomainUserEntity;

public interface ServiceOwneraAware {
    void setServiceOwner(DomainUserEntity user);

    DomainUserEntity getServiceOwner();
}
