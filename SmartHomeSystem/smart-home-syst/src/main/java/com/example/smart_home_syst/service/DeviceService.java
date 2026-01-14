package com.example.smart_home_syst.service;


import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_home_syst.enumerator.DeviceType;
import com.example.smart_home_syst.exception.ResourceNotFoundException;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.repository.DeviceRepository;
import com.example.smart_home_syst.specifications.DeviceSpecifications;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value="devices", key="#root.methodName")
    public List<Device> getAll() {
        return deviceRepository.findAll();
    }

    public List<Device> getAllByTitle(String title) {
        return deviceRepository.findAllByTitle(title);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="device", key="#id")
    public Device getById(Long id) {
        return deviceRepository.findById(id).orElse(null);
    }

    public Device update(Long id, Device device) {
        return deviceRepository.findById(id).map(existingDevice -> {
            existingDevice.setTitle(device.getTitle());
            existingDevice.setMode(device.getMode());
            existingDevice.setRoom(device.getRoom());
            existingDevice.setType(device.getType());
            existingDevice.setPower(device.getPower());
            existingDevice.setActive(device.isActive());
            return deviceRepository.save(existingDevice);
        }).orElseThrow(() -> new ResourceNotFoundException("Error to update device with id: " + id));
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

    public Page<Device> getByFilter (String title, Double min_power, Double max_power, Boolean activity, DeviceType type, Pageable pageable) {
        return deviceRepository.findAll(DeviceSpecifications.filter(title, min_power, max_power, activity, type), pageable);
    }
}
