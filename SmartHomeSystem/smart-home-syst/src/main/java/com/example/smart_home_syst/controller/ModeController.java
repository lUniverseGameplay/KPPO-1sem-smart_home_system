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

import com.example.smart_home_syst.dto.ModeDto;
import com.example.smart_home_syst.enumerator.ModeType;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.service.ModeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(
    name = "Управление режимами работ устройств",
    description = """
    Модуль для управления режимами работ устройств умного дома.
    \nПоддерживаемые типы режимов на данный момент: dinner, morning, study
    """
)
@RestController
public class ModeController {
    private final ModeService modeService;

    ModeController(ModeService modeService) {
        this.modeService = modeService;
    }

    @Operation(
    summary = "Все режимы",
    description = "Получение списка всех режимов работ")
    @GetMapping("/modes")
    public List<Mode> getModes() {
        return modeService.getAll();
    }

    @Operation(
    summary = "Конкретный режим",
    description = "Получение режима с указанным ID")
    @GetMapping("/modes/{id}")
    public ResponseEntity<Mode> getMode(@PathVariable Long id) {
        return ResponseEntity.ok().body(modeService.getById(id));
    }

    @Operation(
    summary = "Новый режим",
    description = """
    Заполните все поля для добавления режима.
    \nTitle - название режима работы
    \nType - тип режима
    """)
    @PostMapping("/modes")
    public ResponseEntity<Mode> addMode(@RequestBody @Valid ModeDto modeDto) {
       Mode newMode = modeService.create(modeDto);
       return  ResponseEntity.status(HttpStatus.CREATED).body(newMode);
    }

    @Operation(
    summary = "Обновление режима",
    description = """
    Заполните все поля для обновления режима, указав его ID.
    \nTitle - название режима работы
    \nType - тип режима
    """)
    @PutMapping("/modes/{id}")
    public ResponseEntity<Mode> editMode(@PathVariable Long id, @RequestBody @Valid ModeDto modeDto) {
        Mode updMode = modeService.update(id, modeDto);
        if(updMode != null) {
            return ResponseEntity.ok(updMode);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
    summary = "Удаление режима",
    description = "Удалить режим с указанным ID")
    @DeleteMapping("/modes/{id}")
    public ResponseEntity <Void> deleteMode(@PathVariable Long id) {
        if (modeService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
    summary = "Режимы по фильтру",
    description = "Заполните поля для вывода списка режима работ, удовлетворяющих требованиям. Для Pageable в поле sort указать 1 поле режима для порядка вывода")
    @GetMapping("/modes/filter")
    public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String title, ModeType type, @PageableDefault(page=0, size=10, sort="title") Pageable pageable) {
        return ResponseEntity.ok(modeService.getByFilter(title, type, pageable));
    }

    @Operation(
    summary = "Устройства с режимом Х",
    description = "Получение всех устройств с режимом работы с указанным ID")
    @GetMapping("/mode-devices/{id}")
    public List<Device> getDevicesOfMode(@PathVariable Long id) {
        return modeService.getDevicesOfMode(id);
    }

    @Operation(
    summary = "Выключение режима Х",
    description = "Выключить все устройства с режимом работы c указанным ID")
    @PutMapping("/mode/turnOff/{id}")
    public ResponseEntity<List<Device>> turnOffRoom(@PathVariable Long id) {
        List<Device> updDeviceList = modeService.turnOffDevicesOfMode(id);
        if(updDeviceList.size() != 0) {
            return ResponseEntity.ok(updDeviceList);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
    summary = "Включение режима Х",
    description = "Включить все устройства с режимом работы c указанным ID")
    @PutMapping("/mode/turnOn/{id}")
    public ResponseEntity<List<Device>> turnOnRoom(@PathVariable Long id) {
        List<Device> updDeviceList = modeService.turnOnDevicesOfMode(id);
        if(updDeviceList.size() != 0) {
            return ResponseEntity.ok(updDeviceList);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }
}
