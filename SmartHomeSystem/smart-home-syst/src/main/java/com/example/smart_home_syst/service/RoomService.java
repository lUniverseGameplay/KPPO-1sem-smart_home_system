package com.example.smart_home_syst.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_home_syst.model.Room;
import com.example.smart_home_syst.repository.RoomRepository;
import com.example.smart_home_syst.specifications.RoomSpecifications;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
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

    public Room update(Long id, Room room) {
        return roomRepository.findById(id).map(existingRoom -> {
            existingRoom.setTitle(room.getTitle());
            existingRoom.setLocation(room.getLocation());
            existingRoom.setCapacity(room.getCapacity());
            return roomRepository.save(existingRoom);
        }).orElseThrow(null);
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
    public Room create (Room room) {
        return roomRepository.save(room);
    }

    public Page<Room> getByFilter(String title, String location, Integer max_capacity, Integer min_capacity, Pageable pageable) {
        return roomRepository.findAll(RoomSpecifications.filter(title, location, max_capacity, min_capacity), pageable);
    }
}
