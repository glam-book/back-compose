package com.tlback.domain.utils;

import com.tlback.domain.DomainUserEntity;

public interface ServiceOwneraAware {
    void setServiceOwner(DomainUserEntity user);
    DomainUserEntity getServiceOwner();
}
