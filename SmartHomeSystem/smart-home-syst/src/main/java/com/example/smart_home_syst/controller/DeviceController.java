package com.example.smart_home_syst.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_home_syst.model.Device;

import jakarta.validation.Valid;


@RestController
public class DeviceController {
    private List<Device> devices = new ArrayList<>(Arrays.asList(
        new Device(1l, "kitchen_lamp", 50.0, true),
        new Device(2l, "bathroom_lamp", 0.5, false)
    ));

    @GetMapping("/devices")
    public List<Device> getDevices() {
        return devices;
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<Device> getProduct(@PathVariable Long id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                return ResponseEntity.ok(device);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/devices")
    public ResponseEntity<Device> addDevice(@RequestBody @Valid Device device) {
        device.setId((long)devices.size() + 1);
        devices.add(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }
}
