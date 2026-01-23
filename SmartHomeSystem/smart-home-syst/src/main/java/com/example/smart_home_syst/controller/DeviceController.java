package com.example.smart_home_syst.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.smart_home_syst.dto.DeviceDto;
import com.example.smart_home_syst.enumerator.DeviceType;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.service.DeviceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@Tag(
    name = "Управление устройствами",
    description = """
    Модуль для управления устройствами умного дома.
    \nПоддерживаемые типы устройств на данный момент: light, coffee_machine, speakers, kettle, microwave, door, conditioner, printer.
    """
)
@RestController
public class DeviceController {
    private final DeviceService deviceService;

    DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(
    summary = "Все устройства",
    description = "Получение списка всех устройств")
    @GetMapping("/devices")
    public List<Device> getDevices() {
        return deviceService.getAll();
    }

    @Operation(
    summary = "Конкретное устройство",
    description = "Получение устройства с указанным ID")
    @GetMapping("/devices/{id}")
    public ResponseEntity<Device> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok().body(deviceService.getById(id)); 
    }

    @Operation(
    summary = "Новое устройство",
    description = """
    Заполните все поля для добавления устройства.
    \nTitle - название устройства
    \nType - тип устройства
    \nPower - напряжение (В)
    \nActive - работает ли устройство (true/false)
    \nModeId - номер режима, в котором устройство работает
    \nRoomId - номер комнаты, где установлено устройство
    """)
    @PostMapping("/devices")
    public ResponseEntity<Device> addDevice(@RequestBody @Valid DeviceDto deviceDto) {
       Device newDevice = deviceService.create(deviceDto);
       return  ResponseEntity.status(HttpStatus.CREATED).body(newDevice);
    }

    @Operation(
    summary = "Обновить устройство",
    description = """
    Заполните все поля для обновления устройства, указав его ID.
    \nTitle - название устройства
    \nType - тип устройства
    \nPower - напряжение (В)
    \nActive - работает ли устройство (true/false)
    \nModeId - номер режима, в котором устройство работает
    \nRoomId - номер комнаты, где установлено устройство
    """)
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
    summary = "Удалить устройство",
    description = "Удаление устройства с указанным ID")
    @DeleteMapping("/devices/{id}")
    public ResponseEntity <Void> deleteDevice(@PathVariable Long id) {
        if (deviceService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
    summary = "Устройства по фильтру",
    description = "Заполните поля для вывода списка устройств, удовлетворяющих требованиям. Для Pageable в поле sort указать 1 поле устройства для порядка вывода")
    @GetMapping("/devices/filter")
    public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String title,
    Double min_power, Double max_power, Boolean activity, DeviceType type, @PageableDefault(page=0, size=10, sort="id") Pageable pageable) {
        return ResponseEntity.ok(deviceService.getByFilter(title, min_power, max_power, activity, type, pageable));
    }

    @Operation(
    summary = "Включение устройств типа Х",
    description = "Включить все устройства с указанным типом устройства")
    @PutMapping("/devices/turnOnType/{type}")
    public ResponseEntity<List<Device>> turnOnDevicesWithType(DeviceType type) {
        List<Device> updDeviceList = deviceService.turnOnDevicesWithType(type);
        if(updDeviceList.size() != 0) {
            return ResponseEntity.ok(updDeviceList);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
    summary = "Выключение устройств типа Х",
    description = "Выключить все устройства с указанным типом устройства")
    @PutMapping("/devices/turnOffType/{type}")
    public ResponseEntity<List<Device>> turnOffDevicesWithType(DeviceType type) {
        List<Device> updDeviceList = deviceService.turnOffDevicesWithType(type);
        if(updDeviceList.size() != 0) {
            return ResponseEntity.ok(updDeviceList);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
    summary = "Экспорт устройств",
    description = "Экспортировать данные обо всех устройствах в файл в формате XML")
    @GetMapping("/devices/export/{type}")
    public ResponseEntity<String> exportDevicesToXml(String name) {
        String path = "exports/xml/devices";
        String savedXml = deviceService.exportDevicesListToXmlFile(path, name);
        if(savedXml != "" & savedXml != null) {
            return ResponseEntity.ok(savedXml);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(path = "/devices/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Device>> importDevicesFromXml(@RequestParam MultipartFile file) {
        List<Device> updDeviceList = deviceService.importDevicesListFromXmlFile(file);
        if(updDeviceList.size() != 0) {
            return ResponseEntity.ok(updDeviceList);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }
    
}
