package com.example.SmartHomeSystem.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartHomeSystem.model.Room;
import com.example.SmartHomeSystem.service.RoomService;

import jakarta.validation.Valid;

@RestController
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/rooms")
    public List<Room> getRooms() {
        return roomService.getAll();
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok().body(roomService.getById(id));
    }
    
    @PutMapping("/rooms/{id}")
    public ResponseEntity<Room> edit(@PathVariable Long id, @RequestBody Room room) {
        Room updated = roomService.update(id, room);
        if(updated != null){
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if(roomService.deleteById(id)){
            return ResponseEntity.noContent().build();
        }
         return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms")
    public ResponseEntity<Room> addRoom(@RequestBody @Valid Room room) {
        Room newRoom = roomService.create(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRoom);
    }
}
