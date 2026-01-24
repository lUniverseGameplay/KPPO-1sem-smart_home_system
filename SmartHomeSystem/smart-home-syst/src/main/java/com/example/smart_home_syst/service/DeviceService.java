package com.example.smart_home_syst.service;


import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.smart_home_syst.dto.DeviceDto;
import com.example.smart_home_syst.dto.DeviceListImportDto;
import com.example.smart_home_syst.dto.DeviceReportDto;
import com.example.smart_home_syst.enumerator.DeviceType;
import com.example.smart_home_syst.exception.ResourceNotFoundException;
import com.example.smart_home_syst.fileSettings.DevicesExportWrapper;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.model.Room;
import com.example.smart_home_syst.repository.DeviceRepository;
import com.example.smart_home_syst.repository.ModeRepository;
import com.example.smart_home_syst.repository.RoomRepository;
import com.example.smart_home_syst.specifications.DeviceSpecifications;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final ModeRepository modeRepository;
    private final RoomRepository roomRepository;
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    private final XmlMapper xmlMapper = new XmlMapper();
    
    @Value("${spring.servlet.multipart.location}")
    private String uploadLocation;

    public DeviceService(DeviceRepository deviceRepository, ModeRepository modeRepository, RoomRepository roomRepository) {
        this.deviceRepository = deviceRepository;
        this.modeRepository = modeRepository;
        this.roomRepository = roomRepository;
        xmlMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="devices", key="#root.methodName")
    public List<Device> getAll() {
        return deviceRepository.findAll();
    }

    public List<Device> getAllByTitle(String title) {
        return deviceRepository.findAllByTitle(title);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="device", key="#id")
    public Device getById(Long id) {
        return deviceRepository.findById(id).orElse(null);
    }

    @Caching(evict = {
        @CacheEvict(value="devices", allEntries=true),
        @CacheEvict(value="device", key="#id")
    })
    @Transactional
    public Device update(Long id, DeviceDto deviceDto) {
        Device existingDevice = deviceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        existingDevice.setTitle(deviceDto.title());
        existingDevice.setType(deviceDto.type());
        existingDevice.setPower(deviceDto.power());
        
        if (deviceDto.active() != null) {
            existingDevice.setActive(deviceDto.active()); // т.к. у Dto нет isActive
        }
        
        if (deviceDto.modeId() != null) {
            Mode mode = modeRepository.findById(deviceDto.modeId()).orElseThrow(() -> new ResourceNotFoundException("Mode not found with id: " + deviceDto.modeId()));
            existingDevice.setMode(mode);
        }
        
        if (deviceDto.roomId() != null) {
            Room room = roomRepository.findById(deviceDto.roomId()).orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + deviceDto.roomId()));
            if (room.getDevices().size() < room.getCapacity() || deviceDto.roomId() == getById(id).getRoomId()) { // проверка того, что количество устройств в комнате < вместимости, или того, что устройство остаётся в комнате
                //if (deviceDto.roomId() == getById(id).getRoomId()) {System.out.println("Устройство на месте");}
                existingDevice.setRoom(room);
            }
            else {
                existingDevice.setRoom(null);
            }
        }
        
        return deviceRepository.save(existingDevice);
    }

    @Caching(evict = {
        @CacheEvict(value="devices", allEntries=true),
        @CacheEvict(value="device", key="#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (deviceRepository.existsById(id)) {
            deviceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public Device create (DeviceDto deviceDto) {
        Device device = new Device();
        device.setTitle(deviceDto.title());
        device.setType(deviceDto.type());
        device.setPower(deviceDto.power());
        
        if (deviceDto.active() != null) {
            device.setActive(deviceDto.active()); // т.к. у Dto нет isActive
        }
        else {
            device.setActive(false);
        }
        
        if (deviceDto.modeId() != null) {
            Mode mode = modeRepository.findById(deviceDto.modeId()).orElseThrow(() -> new ResourceNotFoundException("Mode not found with id: " + deviceDto.modeId()));
            device.setMode(mode);
        }
        else {
            device.setMode(null);
        }
        
        if (deviceDto.roomId() != null) {
            Room room = roomRepository.findById(deviceDto.roomId()).orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + deviceDto.roomId()));
            if (room.getDevices().size() < room.getCapacity()) {
                device.setRoom(room);
            }
            else {
                device.setRoom(null);
            }
        }
        else {
            device.setRoom(null);
        }

        return deviceRepository.save(device);
    }

    public Page<Device> getByFilter (String title, Double min_power, Double max_power, Boolean activity, DeviceType type, Pageable pageable) {
        return deviceRepository.findAll(DeviceSpecifications.filter(title, min_power, max_power, activity, type), pageable);
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOnDevicesWithType(DeviceType type) {
        List<Device> device_to_change = deviceRepository.findAllByType(type);
        for (Device dev : device_to_change) {
            dev.setActive(true);
            deviceRepository.save(dev);
        }
        return device_to_change;
    }
    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOffDevicesWithType(DeviceType type) {
        List<Device> device_to_change = deviceRepository.findAllByType(type);
        for (Device dev : device_to_change) {
            dev.setActive(false);
            deviceRepository.save(dev);
        }
        return device_to_change;
    }

    private DevicesExportWrapper createExportWrapper(List<Device> devices) {
        List<DeviceDto> devicesDto_to_export = devices.stream()
            .map(device -> new DeviceDto(
                device.getTitle(),
                device.getType(),
                device.getPower(),
                device.isActive(),
                device.getMode().getId(),
                device.getRoom().getId()
            ))
            .toList();
        
        DevicesExportWrapper wrapper = new DevicesExportWrapper();
        wrapper.setDevices(devicesDto_to_export);
        wrapper.setTotalCount(devicesDto_to_export.size());
        return wrapper;
    }

    @Transactional(readOnly = true)
    public String exportDevicesListToXmlString() {
        logger.info("Start device exporting to string operation");

        List<Device> devices_to_export = deviceRepository.findAll();
        logger.debug("{} devices was founded", devices_to_export.size());
        
        DevicesExportWrapper wrapper = createExportWrapper(devices_to_export);
        
        try {
            String xml = xmlMapper.writeValueAsString(wrapper);
            logger.debug("XML export to string successfully finished");
            return xml;
        } catch (Exception e) {
            logger.warn("Formating to XML error: {}", e.getMessage(), e);
            throw new RuntimeException("Error of export to XML", e);
        }
    }

    @Transactional(readOnly=true)
    public String exportDevicesListToXmlFile(String pathToFile, String fileName) {
        logger.info("Start device exporting to file operation");

        try {
            // Создаем директорию для экспорта, если её нет
            Path fileDir = Paths.get(pathToFile);
            Files.createDirectories(fileDir);
            Path filePath = fileDir.resolve(fileName + ".xml"); // Соединяем путь и имя файла
            logger.debug("Full path to file created: {}", filePath);

            String xml = exportDevicesListToXmlString();

            logger.debug("Try to write XML string to file: {}", filePath);
            Files.writeString(filePath, xml);
            
            logger.info("Devices data successfully exported to XML file {}", filePath);
            return xml;
        }
        catch (Exception e) {
            logger.warn("Export to file error: {}", e.getMessage(), e);
            throw new RuntimeException("Error to create file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            logger.warn("Empty file");
            throw new IllegalArgumentException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xml")) {
            logger.warn("Wrong file format detected");
            throw new IllegalArgumentException("The system supports only Xml files");
        }
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> importDevicesListFromXmlFile(MultipartFile file) {
        logger.info("Start device importing from file operation");
        validateFile(file);

        try {
            Path filePath = Paths.get("imports", "devices");
            Files.createDirectories(filePath);
            filePath = filePath.resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + "_" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File copied with path: {}", filePath);

            try (InputStream fileByteStream = file.getInputStream()) {
                DeviceListImportDto xmlDto = xmlMapper.readValue(fileByteStream, DeviceListImportDto.class);
                List<DeviceDto> deviceToImport = xmlDto.getDevices();

                List<Device> importedDevices = new ArrayList<>();

                for (int i = 0; i < xmlDto.getDevices().size(); i++) {
                    DeviceDto item = xmlDto.getDevices().get(i);
                    List<Device> devicesWithSameName = deviceRepository.findAllByTitle(item.title());
                    try {
                        if (devicesWithSameName.size() == 0 || devicesWithSameName == null) {
                            importedDevices.add(create(item));
                            
                            logger.debug("Device imported to DB: {}", item.title());
                        }
                        else {
                            importedDevices.add(update(devicesWithSameName.get(0).getId(), item));
                            
                            logger.debug("Device updated in DB: {}", item.title());
                        }
                        
                    } catch (Exception e) {
                        logger.warn("Error to add Device {} to DB", item.title());
                    }
                }
                logger.info("Devices data successfully imported from XML file {}", filePath);
                return importedDevices;
            }
            catch (Exception e) {
                logger.warn("Error of opening XML file: {}", e.getMessage(), e);
                throw new RuntimeException("Error of opening XML file", e);
            }
        }
        catch (Exception e) {
            logger.warn("File import error: {}", e.getMessage(), e);
            throw new RuntimeException("Error to create/change devices from file", e);
        }
    }

    @Transactional(readOnly=true)
    public byte[] generateDevicePdfReport() {
        logger.info("Start device report creating");
        try {
            InputStream templateStream = new ClassPathResource("reportPdfConfig/deviceReportConfig.jrxml").getInputStream(); // загрузка шаблона
            logger.debug("Pdf template loaded");

            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            logger.debug("Pdf template compiled");

            List<DeviceReportDto> reportData = deviceRepository.findAll().stream().map(device -> new DeviceReportDto(
                device.getTitle(),
                device.getType().toString(),
                device.getPower(),
                device.isActive(),
                device.getRoom().getTitle(),
                device.getMode().getTitle()
            )).toList();
            logger.debug("Data taken from DB and change to DeviceReportDto");

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            logger.debug("Data about devices converted for writing to pdf");

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Report about devices of Smart Home System");
            parameters.put("generatedBy", SecurityContextHolder.getContext().getAuthentication().getName());
            logger.debug("Pdf parametrs entered");

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, parameters, dataSource);
            
            logger.info("Devices data report successfully created");
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
        catch (Exception e) {
            logger.warn("Device report creating error: {}", e.getMessage(), e);
            throw new RuntimeException("Error to create report about devices", e);
        }
    }
}
