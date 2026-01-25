package com.example.smart_home_syst.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_home_syst.dto.RoomDto;
import com.example.smart_home_syst.exception.ResourceNotFoundException;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.model.Room;
import com.example.smart_home_syst.model.User;
import com.example.smart_home_syst.repository.DeviceRepository;
import com.example.smart_home_syst.repository.ModeRepository;
import com.example.smart_home_syst.repository.RoomRepository;
import com.example.smart_home_syst.repository.UserRepository;
import com.example.smart_home_syst.specifications.RoomSpecifications;

@Service
public class RoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final ModeRepository modeRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private TgBotService botService;

    public RoomService(RoomRepository roomRepository, DeviceRepository deviceRepository, ModeRepository modeRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.modeRepository = modeRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value="rooms", key="#root.methodName")
    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    public List<Room> getAllByTitle(String title) {
        return roomRepository.findAllByTitle(title);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="room", key="#id")
    public Room getById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    public Room update(Long id, RoomDto roomDto) {
        return roomRepository.findById(id).map(existingRoom -> {
            logger.info("Start Update room operation");
            existingRoom.setTitle(roomDto.title());
            existingRoom.setLocation(roomDto.location());
            existingRoom.setCapacity(roomDto.capacity());
            if (roomDto.managerId() != null) {
                User newUser = userRepository.findById(roomDto.managerId()).orElse(null);
                String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
                if (newUser != null) {
                    logger.debug("New room manager {} entered", roomDto.managerId());
                    existingRoom.setManager(newUser);                    
                }
                else {
                    logger.debug("Manager with Id {} doesn't founded. Set 'null' manager", roomDto.managerId());
                    existingRoom.setManager(null);
                }
            }
            else {
                logger.debug("No manager Id {} in request. Set 'null' manager", roomDto.managerId());
                existingRoom.setManager(null);
            }
            logger.info("Update comleted successfully for Room {}", id);
            return roomRepository.save(existingRoom);
        }).orElseThrow(() -> {
            logger.warn("Error to update mode with id: {}", id);
            return new ResourceNotFoundException("Error to update room with id: " + id);
        });
    }

    @Caching(evict = {
        @CacheEvict(value="rooms", allEntries=true),
        @CacheEvict(value="room", key="#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    @CacheEvict(value="rooms", allEntries=true)
    public Room create (RoomDto roomDto) {
        Room room = new Room();
        room.setTitle(roomDto.title());
        room.setLocation(roomDto.location());
        room.setCapacity(roomDto.capacity());
        if (roomDto.managerId() != null) {
            room.setManager(userRepository.findById(roomDto.managerId()).orElse(null));
        }
        else {
            room.setManager(null);
        }
        return roomRepository.save(room);
    }

    public Page<Room> getByFilter(String title, String location, Integer max_capacity, Integer min_capacity, Pageable pageable) {
        return roomRepository.findAll(RoomSpecifications.filter(title, location, max_capacity, min_capacity), pageable);
    }

    
    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> getDevicesInRoom(Long id) {
        return roomRepository.findById(id).orElse(null).getDevices();
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOffDevicesInRoom(Long id) {
        List<Device> device_to_change = roomRepository.findById(id).orElse(null).getDevices();
        for (Device dev : device_to_change) {
            dev.setActive(false);
            deviceRepository.save(dev);
        }
        return device_to_change;
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOnDevicesInRoom(Long id) {
        List<Device> device_to_change = roomRepository.findById(id).orElse(null).getDevices();
        for (Device dev : device_to_change) {
            dev.setActive(true);
            deviceRepository.save(dev);
        }
        return device_to_change;
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> switchDevicesModeInRoom(Long roomId, Long modeId) {
        List<Device> device_to_change = roomRepository.findById(roomId).orElse(null).getDevices();
        Mode newMode = modeRepository.findById(modeId).orElseThrow(() -> new ResourceNotFoundException("Mode not found with id: " + modeId));
        for (Device dev : device_to_change) {
            dev.setMode(newMode);
            deviceRepository.save(dev);
        }
        return device_to_change;
    }
}
