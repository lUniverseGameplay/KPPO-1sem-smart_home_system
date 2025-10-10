package com.example.SmartHomeSystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartHomeSystem.model.Room;

@Repository
public interface RoomRepository extends 
    JpaRepository<Room, Long>{
        List<Room> findByLocationStartingWithIgnoreCase(String location);
        List<Room> findAllByLocation(String location);
}
