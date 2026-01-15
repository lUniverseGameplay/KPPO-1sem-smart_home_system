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

import com.example.smart_home_syst.dto.DeviceDto;
import com.example.smart_home_syst.enumerator.DeviceType;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.service.DeviceService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;



@RestController
public class DeviceController {
    private final DeviceService deviceService;

    DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(
    summary = "Get All devices",
    description = "Here we try to get list of all devices")
    @GetMapping("/devices")
    public List<Device> getDevices() {
        return deviceService.getAll();
    }

    @Operation(
    summary = "Get device with definite Id",
    description = "Specify the device Id")
    @GetMapping("/devices/{id}")
    public ResponseEntity<Device> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok().body(deviceService.getById(id)); 
    }

    @Operation(
    summary = "Create new device",
    description = "Fill in all the fields. Device type at this moment: light, coffee_machine, speakers, kettle, microwave, door, conditioner, printer")
    @PostMapping("/devices")
    public ResponseEntity<Device> addDevice(@RequestBody @Valid DeviceDto deviceDto) {
       Device newDevice = deviceService.create(deviceDto);
       return  ResponseEntity.status(HttpStatus.CREATED).body(newDevice);
    }

    @Operation(
    summary = "Update device",
    description = "Specify the device Id and fill in all the fields. Device type at this moment: light, coffee_machine, speakers, kettle, microwave, door, conditioner, printer")
    @PutMapping("/devices/{id}")
    public ResponseEntity<Device> editDevice(@PathVariable Long id, @RequestBody @Valid DeviceDto deviceDto) {
        Device updDevice = deviceService.update(id, deviceDto);
        if(updDevice != null) {
            return ResponseEntity.ok(updDevice);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
    summary = "Delete device",
    description = "Specify the device Id")
    @DeleteMapping("/devices/{id}")
    public ResponseEntity <Void> deleteDevice(@PathVariable Long id) {
        if (deviceService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
    summary = "Get devices with filters",
    description = "Fill in all the fields and write in pageable sort field 1 field of device for orderings")
    @GetMapping("/devices/filter")
    public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String title,
    Double min_power, Double max_power, Boolean activity, DeviceType type, @PageableDefault(page=0, size=10, sort="id") Pageable pageable) {
        return ResponseEntity.ok(deviceService.getByFilter(title, min_power, max_power, activity, type, pageable));
    }
}
