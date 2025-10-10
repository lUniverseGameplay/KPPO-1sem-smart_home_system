package com.example.SmartHomeSystem.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.SmartHomeSystem.model.Room;
import com.example.SmartHomeSystem.repository.RoomRepository;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    private final List<Room> rooms = new ArrayList<>();

    public List<Room> getAll() {
        return roomRepository.findAll();

    }

    public List<Room> getAllByLocation(String roomname) {
        return roomRepository.findAllByLocation(roomname);
    }

    public Room create(Room room) {
        return roomRepository.save(room);
    }

    public Room getById(Long id) {
        for (Room room : rooms) {
            if (room.getId().equals(id)) {
                return roomRepository.findById(id).orElse(null);
            }
        }
        return null;
    }

    public Room update(Long id, Room room) {
        return roomRepository.findById(id).map(existingRoom -> {
            existingRoom.setLocation(room.getLocation());
            existingRoom.setMode(room.getMode());
            //existingRoom.setRole(room.getRole()); - как сделать?
            return roomRepository.save(existingRoom);
        }).orElse(null);
    }

    public boolean deleteById(Long id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
