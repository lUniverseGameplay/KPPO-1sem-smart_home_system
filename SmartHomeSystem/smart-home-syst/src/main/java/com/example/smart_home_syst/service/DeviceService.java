package com.example.smart_home_syst.service;


import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_home_syst.dto.DeviceDto;
import com.example.smart_home_syst.enumerator.DeviceType;
import com.example.smart_home_syst.exception.ResourceNotFoundException;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.model.Room;
import com.example.smart_home_syst.repository.DeviceRepository;
import com.example.smart_home_syst.repository.ModeRepository;
import com.example.smart_home_syst.repository.RoomRepository;
import com.example.smart_home_syst.specifications.DeviceSpecifications;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final ModeRepository modeRepository;
    private final RoomRepository roomRepository;

    public DeviceService(DeviceRepository deviceRepository, ModeRepository modeRepository, RoomRepository roomRepository) {
        this.deviceRepository = deviceRepository;
        this.modeRepository = modeRepository;
        this.roomRepository = roomRepository;
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

    public Device update(Long id, DeviceDto deviceDto) {
        Device existingDevice = deviceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        existingDevice.setTitle(deviceDto.title());
        existingDevice.setType(deviceDto.type());
        existingDevice.setPower(deviceDto.power());
        
        if (deviceDto.active() != null) {
            existingDevice.setActive(deviceDto.active()); // т.к. у Dto нет isActive
        }
        
        if (deviceDto.modeId() != null) {
            Mode mode = modeRepository.findById(deviceDto.modeId()).orElseThrow(() -> new ResourceNotFoundException("Mode not found with id: " + deviceDto.modeId()));
            existingDevice.setMode(mode);
        }
        
        if (deviceDto.roomId() != null) {
            Room room = roomRepository.findById(deviceDto.roomId()).orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + deviceDto.roomId()));
            existingDevice.setRoom(room);
        }
        
        return deviceRepository.save(existingDevice);
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
    public Device create (DeviceDto deviceDto) {
        Device device = new Device();
        device.setTitle(deviceDto.title());
        device.setType(deviceDto.type());
        device.setPower(deviceDto.power());
        
        if (deviceDto.active() != null) {
            device.setActive(deviceDto.active()); // т.к. у Dto нет isActive
        }
        else {
            device.setActive(false);
        }
        
        if (deviceDto.modeId() != null) {
            Mode mode = modeRepository.findById(deviceDto.modeId()).orElseThrow(() -> new ResourceNotFoundException("Mode not found with id: " + deviceDto.modeId()));
            device.setMode(mode);
        }
        else {
            device.setMode(null);
        }
        
        if (deviceDto.roomId() != null) {
            Room room = roomRepository.findById(deviceDto.roomId()).orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + deviceDto.roomId()));
            device.setRoom(room);
        }
        else {
            device.setRoom(null);
        }

        return deviceRepository.save(device);
    }

    public Page<Device> getByFilter (String title, Double min_power, Double max_power, Boolean activity, DeviceType type, Pageable pageable) {
        return deviceRepository.findAll(DeviceSpecifications.filter(title, min_power, max_power, activity, type), pageable);
    }
}
