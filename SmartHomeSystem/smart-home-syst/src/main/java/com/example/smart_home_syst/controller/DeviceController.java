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

import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.service.DeviceService;

import jakarta.validation.Valid;



@RestController
public class DeviceController {
    private final DeviceService deviceService;

    DeviceController(DeviceService deviceService) {
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

    @PostMapping("/devices")
    public ResponseEntity<Device> addDevice(@RequestBody @Valid Device device) {
       Device newDevice = deviceService.create(device);
       return  ResponseEntity.status(HttpStatus.CREATED).body(newDevice);
    }

    @PutMapping("/devices/{id}")
    public ResponseEntity<Device> editDevice(@PathVariable Long id, @RequestBody @Valid Device device) {
        Device updDevice = deviceService.update(id, device);
        if(updDevice != null) {
            return ResponseEntity.ok(updDevice);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity <Void> deleteDevice(@PathVariable Long id) {
        if (deviceService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String title,
    Double min_power, Double max_power, Boolean activity, @PageableDefault(page=0, size=10, sort="title") Pageable pageable) {
        return ResponseEntity.ok(deviceService.getByFilter(title, min_power, max_power, activity, pageable));
    }
}
