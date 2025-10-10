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

import com.example.SmartHomeSystem.model.Device;
import com.example.SmartHomeSystem.service.DeviceService;

import jakarta.validation.Valid;

@RestController
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/devices")
    public List<Device> getDevices() {
        return deviceService.getAll();
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<Device> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok().body(deviceService.getById(id));
    }
    
    @PutMapping("/devices/{id}")
    public ResponseEntity<Device> edit(@PathVariable Long id, @RequestBody Device device) {
        Device updated = deviceService.update(id, device);
        if(updated != null){
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if(deviceService.deleteById(id)){
            return ResponseEntity.noContent().build();
        }
         return ResponseEntity.ok().build();
    }

    @PostMapping("/devices")
    public ResponseEntity<Device> addDevice(@RequestBody @Valid Device device) {
        Device newDevice = deviceService.create(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDevice);
    }
}
