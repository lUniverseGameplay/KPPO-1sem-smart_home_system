package com.example.smart_home_syst.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_home_syst.dto.ModeDto;
import com.example.smart_home_syst.enumerator.ModeType;
import com.example.smart_home_syst.exception.ResourceNotFoundException;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.repository.DeviceRepository;
import com.example.smart_home_syst.repository.ModeRepository;
import com.example.smart_home_syst.specifications.ModeSpecifications;

@Service
public class ModeService {
    private final ModeRepository modeRepository;
    private final DeviceRepository deviceRepository;

    public ModeService(ModeRepository modeRepository, DeviceRepository deviceRepository) {
        this.modeRepository = modeRepository;
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value="modes", key="#root.methodName")
    public List<Mode> getAll() {
        return modeRepository.findAll();
    }

    public List<Mode> getAllByTitle(String title) {
        return modeRepository.findAllByTitle(title);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="mode", key="#id")
    public Mode getById(Long id) {
        return modeRepository.findById(id).orElse(null);
    }

    public Mode update(Long id, ModeDto modeDto) {
        return modeRepository.findById(id).map(existingMode -> {
            existingMode.setTitle(modeDto.title());
            existingMode.setType(modeDto.type());
            return modeRepository.save(existingMode);
        }).orElseThrow(() -> new ResourceNotFoundException("Error to update mode with id: " + id));
    }

    @Caching(evict = {
        @CacheEvict(value="modes", allEntries=true),
        @CacheEvict(value="mode", key="#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (modeRepository.existsById(id)) {
            modeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    @CacheEvict(value="modes", allEntries=true)
    public Mode create (ModeDto modeDto) {
        Mode mode = new Mode();
        mode.setTitle(modeDto.title());
        mode.setType(modeDto.type());
        return modeRepository.save(mode);
    }

    public Page<Mode> getByFilter(String title, ModeType type, Pageable pageable) {
        return modeRepository.findAll(ModeSpecifications.filter(title, type), pageable);
    }

    
    @Transactional(readOnly = true)
    @Cacheable(value="mode", key="#id")
    public List<Device> getDevicesOfMode(Long id) {
        return modeRepository.findById(id).orElse(null).getDevices();
    }

    
    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOffDevicesOfMode(Long id) {
        List<Device> device_to_change = modeRepository.findById(id).orElse(null).getDevices();
        for (Device dev : device_to_change) {
            dev.setActive(false);
            deviceRepository.save(dev);
        }
        return device_to_change;
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOnDevicesOfMode(Long id) {
        List<Device> device_to_change = modeRepository.findById(id).orElse(null).getDevices();
        for (Device dev : device_to_change) {
            dev.setActive(true);
            deviceRepository.save(dev);
        }
        return device_to_change;
    }
}
