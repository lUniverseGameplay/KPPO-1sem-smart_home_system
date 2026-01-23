package com.example.smart_home_syst.service;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.example.smart_home_syst.exports.DevicesExportWrapper;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.model.Room;
import com.example.smart_home_syst.repository.DeviceRepository;
import com.example.smart_home_syst.repository.ModeRepository;
import com.example.smart_home_syst.repository.RoomRepository;
import com.example.smart_home_syst.specifications.DeviceSpecifications;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final ModeRepository modeRepository;
    private final RoomRepository roomRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final XmlMapper xmlMapper = new XmlMapper();

    public DeviceService(DeviceRepository deviceRepository, ModeRepository modeRepository, RoomRepository roomRepository) {
        this.deviceRepository = deviceRepository;
        this.modeRepository = modeRepository;
        this.roomRepository = roomRepository;
        xmlMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
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

    @Caching(evict = {
        @CacheEvict(value="devices", allEntries=true),
        @CacheEvict(value="device", key="#id")
    })
    @Transactional
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
            if (room.getDevices().size() < room.getCapacity() || deviceDto.roomId() == getById(id).getRoomId()) { // проверка того, что количество устройств в комнате < вместимости, или того, что устройство остаётся в комнате
                //if (deviceDto.roomId() == getById(id).getRoomId()) {System.out.println("Устройство на месте");}
                existingDevice.setRoom(room);
            }
            else {
                existingDevice.setRoom(null);
            }
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
            if (room.getDevices().size() < room.getCapacity()) {
                device.setRoom(room);
            }
            else {
                device.setRoom(null);
            }
        }
        else {
            device.setRoom(null);
        }

        return deviceRepository.save(device);
    }

    public Page<Device> getByFilter (String title, Double min_power, Double max_power, Boolean activity, DeviceType type, Pageable pageable) {
        return deviceRepository.findAll(DeviceSpecifications.filter(title, min_power, max_power, activity, type), pageable);
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOnDevicesWithType(DeviceType type) {
        List<Device> device_to_change = deviceRepository.findAllByType(type);
        for (Device dev : device_to_change) {
            dev.setActive(true);
            deviceRepository.save(dev);
        }
        return device_to_change;
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOffDevicesWithType(DeviceType type) {
        List<Device> device_to_change = deviceRepository.findAllByType(type);
        for (Device dev : device_to_change) {
            dev.setActive(false);
            deviceRepository.save(dev);
        }
        return device_to_change;
    }

    private DevicesExportWrapper createExportWrapper(List<Device> devices) {
        List<DeviceDto> devicesDto_to_export = devices.stream()
            .map(device -> new DeviceDto(
                device.getTitle(),
                device.getType(),
                device.getPower(),
                device.isActive(),
                device.getMode().getId(),
                device.getRoom().getId()
            ))
            .toList();
        
        DevicesExportWrapper wrapper = new DevicesExportWrapper();
        wrapper.setDevices(devicesDto_to_export);
        wrapper.setTotalCount(devicesDto_to_export.size());
        return wrapper;
    }

    @Transactional(readOnly = true)
    public String exportDevicesListToXmlString() {
        logger.info("Start device exporting to string operation");

        List<Device> devices_to_export = deviceRepository.findAll();
        logger.debug("{} devices was founded", devices_to_export.size());
        
        DevicesExportWrapper wrapper = createExportWrapper(devices_to_export);
        
        try {
            String xml = xmlMapper.writeValueAsString(wrapper);
            logger.debug("XML export to string successfully finished");
            return xml;
        } catch (Exception e) {
            logger.warn("Formating to XML error: {}", e.getMessage(), e);
            throw new RuntimeException("Error of export to XML", e);
        }
    }

    @Transactional(readOnly=true)
    public String exportDevicesListToXmlFile(String pathToFile, String fileName) {
        logger.info("Start device exporting to file operation");

        try {
            // Создаем директорию для экспорта, если её нет
            Path fileDir = Paths.get(pathToFile);
            Files.createDirectories(fileDir);
            Path filePath = fileDir.resolve(fileName + ".xml"); // Соединяем путь и имя файла
            logger.debug("Full path to file created: {}", filePath);

            String xml = exportDevicesListToXmlString();

            logger.debug("Try to write XML string to file: {}", filePath);
            Files.writeString(filePath, xml);
            
            logger.info("Devices data successfully exported to XML file {}", filePath);
            return xml;
        }
        catch (Exception e) {
            logger.warn("Export to file error: {}", e.getMessage(), e);
            throw new RuntimeException("Error to create file", e);
        }
    }
}
