package com.example.smart_home_syst.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
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

import com.example.smart_home_syst.dto.RoomDto;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.model.Room;
import com.example.smart_home_syst.service.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(
    name = "Управление комнатами",
    description = "Модуль для управления комнатами умного дома."
)
@RestController
public class RoomController {
    private final RoomService roomService;

    RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(
    summary = "Все комнаты",
    description = "Получение списка всех комнат")
    @GetMapping("/rooms")
    public List<Room> getRooms() {
        return roomService.getAll();
    }

    @Operation(
    summary = "Конкретная комната",
    description = "Получение комнаты с указанным ID")
    @GetMapping("/rooms/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok().body(roomService.getById(id));
    }

    @Operation(
    summary = "Новая комната",
    description = """
    Заполните все поля для добавления комнаты.
    \nTitle - название комнаты
    \nLocation - расположение комнаты в доме
    \nCapacity - максимальное количество устройств в комнате
    \nmanagerId - Id управляющего комнатой
    """)
    @PostMapping("/rooms")
    public ResponseEntity<Room> addRoom(@RequestBody @Valid RoomDto roomDto) {
       Room newRoom = roomService.create(roomDto);
       return  ResponseEntity.status(HttpStatus.CREATED).body(newRoom);
    }

    @Operation(
    summary = "Обновление комнаты",
    description = """
    Заполните все поля для обновления комнаты.
    \nTitle - название комнаты
    \nLocation - расположение комнаты в доме
    \nCapacity - максимальное количество устройств в комнате
    \nmanagerId - Id управляющего комнатой
    """)
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
    summary = "Удалить комнату",
    description = "Удаление комнаты с указанным ID")
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity <Void> deleteRoom(@PathVariable Long id) {
        if (roomService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
    summary = "Комнаты по фильтру",
    description = "Заполните поля для вывода списка комнат, удовлетворяющих требованиям. Для Pageable в поле sort указать 1 поле комнаты для порядка вывода")
    @GetMapping("/rooms/filter")
    public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String title, String location, Integer max_capacity, Integer min_capacity,
    @PageableDefault(page=0, size=10, sort="title") Pageable pageable) {
        return ResponseEntity.ok(roomService.getByFilter(title, location, max_capacity, min_capacity, pageable));
    }

    @Operation(
    summary = "Устройства в комнате",
    description = "Получение всех устройств в комнате с указанным ID")
    @GetMapping("/room-devices/{id}")
    public List<Device> getDevicesInRoom(@PathVariable Long id) {
        return roomService.getDevicesInRoom(id);
    }

    @Operation(
    summary = "Выключение комнаты",
    description = "Выключить все устройства в комнате c указанным ID")
    @PutMapping("/room/turnOff/{id}")
    public ResponseEntity<List<Device>> turnOffRoom(@PathVariable Long id) {
        List<Device> updDeviceList = roomService.turnOffDevicesInRoom(id);
        if(updDeviceList.size() != 0) {
            return ResponseEntity.ok(updDeviceList);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
    summary = "Включение комнаты",
    description = "Включить все устройства в комнате c указанным ID")
    @PutMapping("/room/turnOn/{id}")
    public ResponseEntity<List<Device>> turnOnRoom(@PathVariable Long id) {
        List<Device> updDeviceList = roomService.turnOnDevicesInRoom(id);
        if(updDeviceList.size() != 0) {
            return ResponseEntity.ok(updDeviceList);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
    summary = "Смена режима в комнате",
    description = "Перевести все устройства в комнате с указанным ID в режим работы с указанным ID")
    @PutMapping("/room/set-mode/{id}")
    public ResponseEntity<List<Device>> switchModeInRoom(Long roomId, Long modeId) {
        List<Device> updDeviceList = roomService.switchDevicesModeInRoom(roomId, modeId);
        if(updDeviceList.size() != 0) {
            return ResponseEntity.ok(updDeviceList);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }
 
    @Operation(
    summary = "Экспорт комнат",
    description = "Экспортировать данные обо всех комнатах в файл в формате XML. Введите название конечного файла перед отправкой запроса")
    @GetMapping("/rooms/export/{type}")
    public ResponseEntity<String> exportRoomsToXml(String filename) {
        String path = "exports/xml/rooms";
        String savedXml = roomService.exportRoomsListToXmlFile(path, filename);
        if(savedXml != "" & savedXml != null) {
            return ResponseEntity.ok(savedXml);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
    summary = "Импорт комнат",
    description = """
        Импортировать данные о комнатах из XML файла.
        Требования к файлу: формат XML, размер не более 10Mb
        Структура файла (текст за // писать не требуется - это комментарии):
        \nsystem>Наименование системы, Имя пользователя экспортера, Формат (писать: '<format>XML</format>')</system
        \nroom>
        \n  title>Название комнаты</title
        \n  location>Расположение комнаты в доме</location
        \n  capacity>Максимальное количество устройств в комнате</capacity // Должно быть число
        \n  managerId>Id пользователя, назначенного менеджером</managerId // Должно быть, соответствующее Id пользователя
        \n</room"
    """)
    @PostMapping(path = "/rooms/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Room>> importRoomsFromXml(@RequestParam MultipartFile file) {
        List<Room> updRoomList = roomService.importRoomsListFromXmlFile(file);
        if(updRoomList.size() != 0) {
            return ResponseEntity.ok(updRoomList);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }
        
    @Operation(
    summary = "Отчёт о комнатах",
    description = "Сформировать отчёт обо всех комнатах в доме (берётся из БД). Результатом будет pdf файл, который можно будет скачать по ссылке")
    @GetMapping("/rooms/report/{type}")
    public ResponseEntity<ByteArrayResource> generateRoomsPdf() {
        byte[] pdfContent = roomService.generateRoomPdfReport();
        String filename = "rooms_report_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".pdf";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfContent.length)
            .body(new ByteArrayResource(pdfContent));
    }
        
    @Operation(
    summary = "Уведомить менеджера комнаты",
    description = "Отправить сообщение менеджеру комнаты с указанным номером через бота")
    @GetMapping("/room/sendMessage/{id}")
    public ResponseEntity<String> sendMessageManager(Long roomId, String message) {
        return ResponseEntity.ok(roomService.notifyRoomManager(roomId, message));
    }
}
