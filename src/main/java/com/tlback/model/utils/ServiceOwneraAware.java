package com.tlback.model.utils;

import com.tlback.model.DomainUserEntity;

public interface ServiceOwneraAware {
    void setServiceOwner(DomainUserEntity user);

    DomainUserEntity getServiceOwner();
}
