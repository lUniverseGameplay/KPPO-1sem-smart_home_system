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

import com.example.smart_home_syst.dto.RoomDto;
import com.example.smart_home_syst.model.Room;
import com.example.smart_home_syst.service.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;


@RestController
public class RoomController {
    private final RoomService roomService;

    RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(
    summary = "Get All rooms",
    description = "Here we try to get list of all rooms")
    @GetMapping("/rooms")
    public List<Room> getRooms() {
        return roomService.getAll();
    }

    @Operation(
    summary = "Get room with definite Id",
    description = "Specify the room Id")
    @GetMapping("/rooms/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok().body(roomService.getById(id));
    }

    @Operation(
    summary = "Create new room",
    description = "Fill in all the fields")
    @PostMapping("/rooms")
    public ResponseEntity<Room> addRoom(@RequestBody @Valid RoomDto roomDto) {
       Room newRoom = roomService.create(roomDto);
       return  ResponseEntity.status(HttpStatus.CREATED).body(newRoom);
    }

    @Operation(
    summary = "Update room",
    description = "Specify the room Id and fill in all the fields")
    @PutMapping("/rooms/{id}")
    public ResponseEntity<Room> editRoom(@PathVariable Long id, @RequestBody @Valid RoomDto roomDto) {
        Room updRoom = roomService.update(id, roomDto);
        if(updRoom != null) {
            return ResponseEntity.ok(updRoom);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
    summary = "Delete room",
    description = "Specify the room Id")
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity <Void> deleteRoom(@PathVariable Long id) {
        if (roomService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
    summary = "Get rooms with filters",
    description = "Fill in all the fields and write in pageable sort field 1 field of room for orderings")
    @GetMapping("/rooms/filter")
    public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String title, String location, Integer max_capacity, Integer min_capacity,
    @PageableDefault(page=0, size=10, sort="title") Pageable pageable) {
        return ResponseEntity.ok(roomService.getByFilter(title, location, max_capacity, min_capacity, pageable));
    }
}
