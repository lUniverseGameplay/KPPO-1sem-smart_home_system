package com.example.smart_home_syst.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_home_syst.model.Room;
import com.example.smart_home_syst.service.RoomService;

import jakarta.validation.Valid;


@RestController
public class RoomController {
    private final RoomService roomService;

    RoomController(RoomService roomService) {
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

    @PostMapping("/rooms")
    public ResponseEntity<Room> addRoom(@RequestBody @Valid Room room) {
       Room newRoom = roomService.create(room);
       return  ResponseEntity.status(HttpStatus.CREATED).body(newRoom);
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<Room> editRoom(@PathVariable Long id, @RequestBody @Valid Room room) {
        Room updRoom = roomService.update(id, room);
        if(updRoom != null) {
            return ResponseEntity.ok(updRoom);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity <Void> deleteMode(@PathVariable Long id) {
        if (roomService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/filter")
    public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String title, String location, Integer max_capacity, Integer min_capacity,
    @PageableDefault(page=0, size=10, sort="title") Pageable pageable) {
        return ResponseEntity.ok(roomService.getByFilter(title, location, max_capacity, min_capacity, pageable));
    }
}
