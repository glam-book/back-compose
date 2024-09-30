package com.tlback.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tlback.domain.ServiceOwnerInfo;
import com.tlback.domain.User;
import com.tlback.repos.ServiceOwnerInfoRepository;
import com.tlback.service.OwnerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MockOwnerServiceImpl implements OwnerService {
    private final User user;
    private final ServiceOwnerInfoRepository repository;

    @Override
    public ServiceOwnerInfo createService(String name) {
        var service = new ServiceOwnerInfo();
        service.setServiceName(name);
        service.setServiceDescription("Go v civy");
        service.setTimeDuration(2);
        service.setServiceOwner(user);

        return repository.save(service);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOwnerInfo findServiceById(Long serviceId) {
        return repository.findById(serviceId)
            .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + serviceId));
    }
}
