package com.example.SmartHomeSystem.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.SmartHomeSystem.model.Device;
import com.example.SmartHomeSystem.repository.DeviceRepository;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    private final List<Device> devices = new ArrayList<>();

    public List<Device> getAll() {
        return deviceRepository.findAll();

    }

    public List<Device> getAllByTitle(String title) {
        return deviceRepository.findAllByTitle(title);
    }

    public Device create(Device device) {
        return deviceRepository.save(device);
    }

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
            existingDevice.setRoom(device.getRoom());
            existingDevice.setPower(device.getPower());
            //existingDevice.setRole(device.getRole()); - как сделать?
            return deviceRepository.save(existingDevice);
        }).orElse(null);
    }

    public boolean deleteById(Long id) {
        if (deviceRepository.existsById(id)) {
            deviceRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
