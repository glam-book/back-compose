package com.tlback.domain.utils;

import com.tlback.domain.UserEntity;

public interface ServiceOwneraAware {
    void setServiceOwner(UserEntity user);
    UserEntity getServiceOwner();
}
