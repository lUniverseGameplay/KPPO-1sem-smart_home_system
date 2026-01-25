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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    @PreAuthorize("hasAuthority('MODE:READ')")
    @GetMapping("/modes")
    public List<Mode> getModes() {
        return modeService.getAll();
    }

    @Operation(
    summary = "Конкретный режим",
    description = "Получение режима с указанным ID")
    @PreAuthorize("hasAuthority('MODE:READ')")
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
    @PreAuthorize("hasAuthority('MODE:CREATE')")
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
    @PreAuthorize("hasAuthority('MODE:UPDATE')")
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
    @PreAuthorize("hasAuthority('MODE:DELETE')")
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
    @PreAuthorize("hasAuthority('MODE:READ')")
    @GetMapping("/modes/filter")
    public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String title, ModeType type, @PageableDefault(page=0, size=10, sort="title") Pageable pageable) {
        return ResponseEntity.ok(modeService.getByFilter(title, type, pageable));
    }

    @Operation(
    summary = "Устройства с режимом Х",
    description = "Получение всех устройств с режимом работы с указанным ID")
    @PreAuthorize("hasAuthority('DEVICE:READ')")
    @GetMapping("/mode-devices/{id}")
    public List<Device> getDevicesOfMode(@PathVariable Long id) {
        return modeService.getDevicesOfMode(id);
    }

    @Operation(
    summary = "Выключение режима Х",
    description = "Выключить все устройства с режимом работы c указанным ID")
    @PreAuthorize("hasAuthority('DEVICE:UPDATE')")
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
    @PreAuthorize("hasAuthority('DEVICE:UPDATE')")
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
    
    @Operation(
    summary = "Экспорт режимов",
    description = "Экспортировать данные обо всех режимах работы устройств в файл в формате XML. Введите название конечного файла перед отправкой запроса")
    @PreAuthorize("hasAuthority('MODE:READ')")
    @GetMapping("/modes/export/{type}")
    public ResponseEntity<String> exportModesToXml(String filename) {
        String path = "exports/xml/modes";
        String savedXml = modeService.exportModesListToXmlFile(path, filename);
        if(savedXml != "" & savedXml != null) {
            return ResponseEntity.ok(savedXml);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
    summary = "Импорт режимов",
    description = """
        Импортировать данные о режимах работ устройств из XML файла.
        Требования к файлу: формат XML, размер не более 10Mb
        Структура файла (текст за // писать не требуется - это комментарии):
        \nsystem>Наименование системы, Имя пользователя экспортера, Формат (писать: '<format>XML</format>')</system
        \nmode>
        \n  title>Название режима</title
        \n  type>Тип режима работы</type // Должен совпадать с ModeType
        \n</mode"
    """)
    @PreAuthorize("hasAuthority('MODE:CREATE')")
    @PostMapping(path = "/modes/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Mode>> importModesFromXml(@RequestParam MultipartFile file) {
        List<Mode> updModeList = modeService.importModesListFromXmlFile(file);
        if(updModeList.size() != 0) {
            return ResponseEntity.ok(updModeList);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }
        
    @Operation(
    summary = "Отчёт об режимах",
    description = "Сформировать отчёт обо всех режимах работы устройствах (данные из БД). Результатом будет pdf файл, который можно будет скачать по ссылке")
    @PreAuthorize("hasAuthority('MODE:READ')")
    @GetMapping("/modes/report/{type}")
    public ResponseEntity<ByteArrayResource> generateModesPdf() {
        byte[] pdfContent = modeService.generateModePdfReport();
        String filename = "modes_report_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".pdf";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfContent.length)
            .body(new ByteArrayResource(pdfContent));
    }
}
