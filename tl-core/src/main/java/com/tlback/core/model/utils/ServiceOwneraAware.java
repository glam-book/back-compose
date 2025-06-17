package com.tlback.core.model.utils;

import com.tlback.core.model.DomainUserEntity;

public interface ServiceOwneraAware {
    void setServiceOwner(DomainUserEntity user);

    DomainUserEntity getServiceOwner();
}
