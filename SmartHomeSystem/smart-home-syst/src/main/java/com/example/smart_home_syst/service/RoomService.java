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

import com.example.smart_home_syst.dto.RoomDto;
import com.example.smart_home_syst.dto.RoomListImportDto;
import com.example.smart_home_syst.dto.RoomReportDto;
import com.example.smart_home_syst.exception.ResourceNotFoundException;
import com.example.smart_home_syst.fileSettings.RoomsExportWrapper;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.model.Room;
import com.example.smart_home_syst.model.User;
import com.example.smart_home_syst.repository.DeviceRepository;
import com.example.smart_home_syst.repository.ModeRepository;
import com.example.smart_home_syst.repository.RoomRepository;
import com.example.smart_home_syst.repository.UserRepository;
import com.example.smart_home_syst.specifications.RoomSpecifications;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class RoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final ModeRepository modeRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final TgBotService botService;
    private final XmlMapper xmlMapper = new XmlMapper();

    public RoomService(RoomRepository roomRepository, DeviceRepository deviceRepository, ModeRepository modeRepository, UserRepository userRepository, TgBotService botService) {
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.modeRepository = modeRepository;
        this.userRepository = userRepository;
        this.botService = botService;
        xmlMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value="rooms", key="#root.methodName")
    public List<Room> getAll() {
        logger.debug("Get all rooms");
        return roomRepository.findAll();
    }

    public List<Room> getAllByTitle(String title) {
        logger.debug("Get all rooms with title: {}", title);
        return roomRepository.findAllByTitle(title);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="room", key="#id")
    public Room getById(Long id) {
        logger.debug("Get room with id: {}", id);
        return roomRepository.findById(id).orElse(null);
    }

    public Room update(Long id, RoomDto roomDto) {
        return roomRepository.findById(id).map(existingRoom -> {
            logger.info("Start Update room operation");
            existingRoom.setTitle(roomDto.title());
            existingRoom.setLocation(roomDto.location());
            existingRoom.setCapacity(roomDto.capacity());
            if (roomDto.managerId() != null) {
                User newUser = userRepository.findById(roomDto.managerId()).orElse(null);
                String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
                if (newUser != null) {
                    logger.debug("New room manager {} entered", roomDto.managerId());
                    existingRoom.setManager(newUser);                    
                }
                else {
                    logger.debug("Manager with Id {} doesn't founded. Set 'null' manager", roomDto.managerId());
                    existingRoom.setManager(null);
                }
            }
            else {
                logger.debug("No manager Id {} in request. Set 'null' manager", roomDto.managerId());
                existingRoom.setManager(null);
            }
            logger.info("Update comleted successfully for Room {}", id);
            return roomRepository.save(existingRoom);
        }).orElseThrow(() -> {
            logger.warn("Error to update mode with id: {}", id);
            return new ResourceNotFoundException("Error to update room with id: " + id);
        });
    }

    @Caching(evict = {
        @CacheEvict(value="rooms", allEntries=true),
        @CacheEvict(value="room", key="#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        logger.info("Start Delete room operation");
        logger.debug("Try to find room with Id {}", id);
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            logger.info("Delete room with Id {} completed successfully", id);
            return true;
        }
        logger.info("Room with Id {} doesn't founded. Operation canceled", id);
        return false;
    }

    @Transactional
    @CacheEvict(value="rooms", allEntries=true)
    public Room create (RoomDto roomDto) {
        logger.info("Start Create room operation");
        Room room = new Room();
        room.setTitle(roomDto.title());
        room.setLocation(roomDto.location());
        room.setCapacity(roomDto.capacity());
        if (roomDto.managerId() != null) {
            room.setManager(userRepository.findById(roomDto.managerId()).orElse(null));
        }
        else {
            logger.debug("No manager Id {} in request. Set 'null' manager", roomDto.managerId());
            room.setManager(null);
        }
        roomRepository.save(room);
        logger.info("Update comleted successfully for Room {}", room.getId());
        return room;
    }

    public Page<Room> getByFilter(String title, String location, Integer max_capacity, Integer min_capacity, Pageable pageable) {
        return roomRepository.findAll(RoomSpecifications.filter(title, location, max_capacity, min_capacity), pageable);
    }

    
    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> getDevicesInRoom(Long id) {
        logger.info("Get all devices in room with Id {}", id);
        return roomRepository.findById(id).orElse(null).getDevices();
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOffDevicesInRoom(Long id) {
        logger.info("Start 'Turn off devices in room' operation");
        List<Device> device_to_change = roomRepository.findById(id).orElse(null).getDevices();
        logger.debug("Founded {} devices in room with Id {}", device_to_change.size(), id);
        for (Device dev : device_to_change) {
            dev.setActive(false);
            deviceRepository.save(dev);
        }
        logger.info("All devices activity in room {} was changed to false", id);
        return device_to_change;
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOnDevicesInRoom(Long id) {
        logger.info("Start 'Turn on devices in room' operation");
        List<Device> device_to_change = roomRepository.findById(id).orElse(null).getDevices();
        logger.debug("Founded {} devices in room with Id {}", device_to_change.size(), id);
        for (Device dev : device_to_change) {
            dev.setActive(true);
            deviceRepository.save(dev);
        }
        logger.info("All devices activity in room {} was changed to true", id);
        return device_to_change;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value="devices", allEntries=true),
        @CacheEvict(value="modes", key="#id")
    })
    public List<Device> switchDevicesModeInRoom(Long roomId, Long modeId) {
        logger.info("Start 'Switch devices mode in room' operation");
        List<Device> device_to_change = roomRepository.findById(roomId).orElse(null).getDevices();
        logger.debug("Founded {} devices in room with Id {}", device_to_change.size(), roomId);
        Mode newMode = modeRepository.findById(modeId).orElseThrow(() -> new ResourceNotFoundException("Mode not found with id: " + modeId));
        for (Device dev : device_to_change) {
            dev.setMode(newMode);
            deviceRepository.save(dev);
        }
        logger.info("All devices mode in room {} was changed to mode {} with Id {}", roomId, newMode.getTitle(), modeId);
        return device_to_change;
    }

    private RoomsExportWrapper createExportWrapper(List<Room> rooms) {
        List<RoomDto> roomsDto_to_export = rooms.stream()
            .map(room -> new RoomDto(
                room.getTitle(),
                room.getLocation(),
                room.getCapacity(),
                room.getManager().getId()
            ))
            .toList();
        
        RoomsExportWrapper wrapper = new RoomsExportWrapper();
        wrapper.setRooms(roomsDto_to_export);
        wrapper.setTotalCount(roomsDto_to_export.size());
        return wrapper;
    }

    @Transactional(readOnly = true)
    public String exportRoomsListToXmlString() {
        logger.info("Start room exporting to string operation");

        List<Room> rooms_to_export = roomRepository.findAll();
        logger.debug("{} rooms was founded", rooms_to_export.size());
        
        RoomsExportWrapper wrapper = createExportWrapper(rooms_to_export);
        
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
    public String exportRoomsListToXmlFile(String pathToFile, String fileName) {
        logger.info("Start rooms exporting to file operation");

        try {
            // Создаем директорию для экспорта, если её нет
            Path fileDir = Paths.get(pathToFile);
            Files.createDirectories(fileDir);
            Path filePath = fileDir.resolve(fileName + ".xml"); // Соединяем путь и имя файла
            logger.debug("Full path to file created: {}", filePath);

            String xml = exportRoomsListToXmlString();

            logger.debug("Try to write XML string to file: {}", filePath);
            Files.writeString(filePath, xml);
            
            logger.info("Rooms data successfully exported to XML file {}", filePath);
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
    @CacheEvict(value="rooms", allEntries=true)
    public List<Room> importRoomsListFromXmlFile(MultipartFile file) {
        logger.info("Start room importing from file operation");
        validateFile(file);

        try {
            Path filePath = Paths.get("imports", "rooms");
            Files.createDirectories(filePath);
            filePath = filePath.resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + "_" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File copied with path: {}", filePath);

            try (InputStream fileByteStream = file.getInputStream()) {
                RoomListImportDto xmlDto = xmlMapper.readValue(fileByteStream, RoomListImportDto.class);
                List<RoomDto> roomToImport = xmlDto.getRooms();

                List<Room> importedRooms = new ArrayList<>();

                for (int i = 0; i < roomToImport.size(); i++) {
                    RoomDto item = roomToImport.get(i);
                    List<Room> roomsWithSameName = roomRepository.findAllByTitle(item.title());
                    try {
                        if (roomsWithSameName.size() == 0 || roomsWithSameName == null) {
                            importedRooms.add(create(item));
                            
                            logger.debug("Room imported to DB: {}", item.title());
                        }
                        else {
                            importedRooms.add(update(roomsWithSameName.get(0).getId(), item));
                            
                            logger.debug("Room updated in DB: {}", item.title());
                        }
                        
                    } catch (Exception e) {
                        logger.warn("Error to add Room {} to DB", item.title());
                    }
                }
                logger.info("Rooms data successfully imported from XML file {}", filePath);
                return importedRooms;
            }
            catch (Exception e) {
                logger.warn("Error of opening XML file: {}", e.getMessage(), e);
                throw new RuntimeException("Error of opening XML file", e);
            }
        }
        catch (Exception e) {
            logger.warn("File import error: {}", e.getMessage(), e);
            throw new RuntimeException("Error to create/change rooms from file", e);
        }
    }

    public String getRoomDevicesNames (Room room) {
        String devicesNames = "";
        for (int i = 0; i < room.getDevices().size(); i++) {
            if (devicesNames != "") {
                devicesNames += ", ";
            }
            devicesNames += room.getDevices().get(i).getTitle();
        }
        return devicesNames;
    }
    
    @Transactional(readOnly=true)
    public byte[] generateRoomPdfReport() {
        logger.info("Start room report creating");
        try {
            InputStream templateStream = new ClassPathResource("reportPdfConfig/roomReportConfig.jrxml").getInputStream(); // загрузка шаблона
            logger.debug("Pdf template loaded");

            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            logger.debug("Pdf template compiled");

            List<RoomReportDto> reportData = roomRepository.findAll().stream().map(room -> new RoomReportDto(
                room.getTitle(),
                room.getLocation(),
                room.getCapacity(),
                room.getManager().getUsername(),
                getRoomDevicesNames(room)
            )).toList();
            logger.debug("Data taken from DB and change to RoomReportDto");

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            logger.debug("Data about rooms converted for writing to pdf");

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Report about rooms of Smart Home System");
            parameters.put("generatedBy", SecurityContextHolder.getContext().getAuthentication().getName());
            logger.debug("Pdf parametrs entered");

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, parameters, dataSource);
            
            logger.info("Rooms data report successfully created");
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
        catch (Exception e) {
            logger.warn("Room report creating error: {}", e.getMessage(), e);
            throw new RuntimeException("Error to create report about rooms", e);
        }
    }

    public String notifyRoomManager(Long roomId, String message) {
        Room currentRoom = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        User manager = currentRoom.getManager();
        if (manager.getTgBotChatId() != null) {
            botService.sendMessage(manager.getTgBotChatId().toString(), message);
            return "Message successfully sended to user " + manager.getUsername();
        }
        return "No chat with manager. Please, ask admin about it";
    }
}
