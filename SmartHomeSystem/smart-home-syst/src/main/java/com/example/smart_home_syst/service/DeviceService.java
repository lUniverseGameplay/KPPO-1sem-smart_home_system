package com.example.smart_home_syst.service;


import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.repository.DeviceRepository;
import com.example.smart_home_syst.specifications.DeviceSpecifications;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    private final List<Device> devices = new ArrayList<>();

    @Transactional(readOnly = true)
    @Cacheable(value="devices", key="#root.methodName")
    public List<Device> getAll() {
        return deviceRepository.findAll();
    }

    public List<Device> getAllByTitle(String title) {
        return deviceRepository.findAllByTitle(title);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="product", key="#id")
    public Device getById(Long id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                return deviceRepository.findById(id).orElse(null);
            }
        }
        return null;
    }

    public Device update(Long id, Device device) {
        return deviceRepository.findById(id).map(existingDevice -> {
            existingDevice.setTitle(device.getTitle());
            existingDevice.setPower(device.getPower());
            existingDevice.setActive(device.isActive());
            return deviceRepository.save(existingDevice);
        }).orElseThrow(null);
    }

    @Caching(evict = {
        @CacheEvict(value="devices", allEntries=true),
        @CacheEvict(value="device", key="#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (deviceRepository.existsById(id)) {
            deviceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public Device create (Device device) {
        return deviceRepository.save(device);
    }

    public Page<Device> getByFilter (String title, Double min_power, Double max_power, Boolean activity, Pageable pageable) {
        return deviceRepository.findAll(DeviceSpecifications.filter(title, min_power, max_power, activity), pageable);
    }
}
