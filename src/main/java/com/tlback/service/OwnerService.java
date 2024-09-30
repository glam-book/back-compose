package com.tlback.service;

import com.tlback.domain.ServiceOwnerInfo;

public interface OwnerService {
    ServiceOwnerInfo createService(String name);

    ServiceOwnerInfo findServiceById(Long serviceId);
}
