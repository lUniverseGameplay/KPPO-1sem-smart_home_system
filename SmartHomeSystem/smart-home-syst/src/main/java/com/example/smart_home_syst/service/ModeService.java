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

import com.example.smart_home_syst.dto.DeviceDto;
import com.example.smart_home_syst.dto.DeviceListImportDto;
import com.example.smart_home_syst.dto.DeviceReportDto;
import com.example.smart_home_syst.dto.ModeDto;
import com.example.smart_home_syst.dto.ModeListImportDto;
import com.example.smart_home_syst.dto.ModeReportDto;
import com.example.smart_home_syst.enumerator.ModeType;
import com.example.smart_home_syst.exception.ResourceNotFoundException;
import com.example.smart_home_syst.fileSettings.ModesExportWrapper;
import com.example.smart_home_syst.model.Device;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.repository.DeviceRepository;
import com.example.smart_home_syst.repository.ModeRepository;
import com.example.smart_home_syst.specifications.ModeSpecifications;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class ModeService {
    private final ModeRepository modeRepository;
    private final DeviceRepository deviceRepository;
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    private final XmlMapper xmlMapper = new XmlMapper();

    public ModeService(ModeRepository modeRepository, DeviceRepository deviceRepository) {
        this.modeRepository = modeRepository;
        this.deviceRepository = deviceRepository;
        xmlMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="modes", key="#root.methodName")
    public List<Mode> getAll() {
        logger.debug("Get all modes");
        return modeRepository.findAll();
    }

    public List<Mode> getAllByTitle(String title) {
        return modeRepository.findAllByTitle(title);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="mode", key="#id")
    public Mode getById(Long id) {
        logger.debug("Get mode with id: {}", id);
        return modeRepository.findById(id).orElse(null);
    }

    @Caching(evict = {
        @CacheEvict(value="modes", allEntries=true),
        @CacheEvict(value="mode", key="#id")
    })
    @Transactional
    public Mode update(Long id, ModeDto modeDto) {
        logger.info("Start Update mode operation");
        return modeRepository.findById(id).map(existingMode -> {
            existingMode.setTitle(modeDto.title());
            existingMode.setType(modeDto.type());
            logger.info("Update comleted successfully for Mode {}", id);
            return modeRepository.save(existingMode);
        }).orElseThrow(() -> {
                logger.warn("Error to update mode with id: {}", id);
                return new ResourceNotFoundException("Error to update mode with id: " + id);
            });
    }

    @Caching(evict = {
        @CacheEvict(value="modes", allEntries=true),
        @CacheEvict(value="mode", key="#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        logger.info("Start Delete mode operation");
        logger.debug("Try to find mode with Id {}", id);
        if (modeRepository.existsById(id)) {
            modeRepository.deleteById(id);
            logger.info("Delete mode with Id {} completed successfully", id);
            return true;
        }
        logger.info("Mode with Id {} doesn't founded. Operation canceled", id);
        return false;
    }

    @Transactional
    @CacheEvict(value="modes", allEntries=true)
    public Mode create (ModeDto modeDto) {
        logger.info("Start Create mode operation");
        Mode mode = new Mode();
        mode.setTitle(modeDto.title());
        mode.setType(modeDto.type());
        logger.info("Mode with Id {} successfully created", mode.getId());
        return modeRepository.save(mode);
    }

    public Page<Mode> getByFilter(String title, ModeType type, Pageable pageable) {
        return modeRepository.findAll(ModeSpecifications.filter(title, type), pageable);
    }

    
    @Transactional(readOnly = true)
    @Cacheable(value="mode", key="#id")
    public List<Device> getDevicesOfMode(Long id) {
        return modeRepository.findById(id).orElse(null).getDevices();
    }

    
    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOffDevicesOfMode(Long id) {
        logger.info("Start 'Turn off devices with same mode' operation");
        List<Device> device_to_change = modeRepository.findById(id).orElse(null).getDevices();
        logger.debug("Founded {} devices with mode {}", device_to_change.size(), modeRepository.findById(id).orElse(null).getTitle());
        for (Device dev : device_to_change) {
            dev.setActive(false);
            deviceRepository.save(dev);
        }
        logger.info("All devices activity with mode {} was changed to false", modeRepository.findById(id).orElse(null).getTitle());
        return device_to_change;
    }

    @Transactional
    @CacheEvict(value="devices", allEntries=true)
    public List<Device> turnOnDevicesOfMode(Long id) {
        logger.info("Start 'Turn on devices with same mode' operation");
        List<Device> device_to_change = modeRepository.findById(id).orElse(null).getDevices();
        logger.debug("Founded {} devices with mode {}", device_to_change.size(), modeRepository.findById(id).orElse(null).getTitle());
        for (Device dev : device_to_change) {
            dev.setActive(true);
            deviceRepository.save(dev);
        }
        logger.info("All devices activity with mode {} was changed to true", modeRepository.findById(id).orElse(null).getTitle());
        return device_to_change;
    }

    private ModesExportWrapper createExportWrapper(List<Mode> modes) {
        List<ModeDto> modesDto_to_export = modes.stream()
            .map(mode -> new ModeDto(
                mode.getTitle(),
                mode.getType()
            ))
            .toList();
        
        ModesExportWrapper wrapper = new ModesExportWrapper();
        wrapper.setModes(modesDto_to_export);
        wrapper.setTotalCount(modesDto_to_export.size());
        return wrapper;
    }

    @Transactional(readOnly = true)
    public String exportModesListToXmlString() {
        logger.info("Start mode exporting to string operation");

        List<Mode> modes_to_export = modeRepository.findAll();
        logger.debug("{} modes was founded", modes_to_export.size());
        
        ModesExportWrapper wrapper = createExportWrapper(modes_to_export);
        
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
    public String exportModesListToXmlFile(String pathToFile, String fileName) {
        logger.info("Start mode exporting to file operation");

        try {
            // Создаем директорию для экспорта, если её нет
            Path fileDir = Paths.get(pathToFile);
            Files.createDirectories(fileDir);
            Path filePath = fileDir.resolve(fileName + ".xml"); // Соединяем путь и имя файла
            logger.debug("Full path to file created: {}", filePath);

            String xml = exportModesListToXmlString();

            logger.debug("Try to write XML string to file: {}", filePath);
            Files.writeString(filePath, xml);
            
            logger.info("Modes data successfully exported to XML file {}", filePath);
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
    @CacheEvict(value="modes", allEntries=true)
    public List<Mode> importModesListFromXmlFile(MultipartFile file) {
        logger.info("Start mode importing from file operation");
        validateFile(file);

        try {
            Path filePath = Paths.get("imports", "modes");
            Files.createDirectories(filePath);
            filePath = filePath.resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + "_" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File copied with path: {}", filePath);

            try (InputStream fileByteStream = file.getInputStream()) {
                ModeListImportDto xmlDto = xmlMapper.readValue(fileByteStream, ModeListImportDto.class);
                List<ModeDto> modeToImport = xmlDto.getModes();

                List<Mode> importedModes = new ArrayList<>();

                for (int i = 0; i < modeToImport.size(); i++) {
                    ModeDto item = modeToImport.get(i);
                    List<Mode> modesWithSameName = modeRepository.findAllByTitle(item.title());
                    try {
                        if (modesWithSameName.size() == 0 || modesWithSameName == null) {
                            importedModes.add(create(item));
                            
                            logger.debug("Mode imported to DB: {}", item.title());
                        }
                        else {
                            importedModes.add(update(modesWithSameName.get(0).getId(), item));
                            
                            logger.debug("Mode updated in DB: {}", item.title());
                        }
                        
                    } catch (Exception e) {
                        logger.warn("Error to add Mode {} to DB", item.title());
                    }
                }
                logger.info("Modes data successfully imported from XML file {}", filePath);
                return importedModes;
            }
            catch (Exception e) {
                logger.warn("Error of opening XML file: {}", e.getMessage(), e);
                throw new RuntimeException("Error of opening XML file", e);
            }
        }
        catch (Exception e) {
            logger.warn("File import error: {}", e.getMessage(), e);
            throw new RuntimeException("Error to create/change modes from file", e);
        }
    }

    public String getModeDevicesNames (Mode mode) {
        String devicesNames = "";
        for (int i = 0; i < mode.getDevices().size(); i++) {
            if (devicesNames != "") {
                devicesNames += ", ";
            }
            devicesNames += mode.getDevices().get(i).getTitle();
        }
        return devicesNames;
    }

    @Transactional(readOnly=true)
    public byte[] generateModePdfReport() {
        logger.info("Start mode report creating");
        try {
            InputStream templateStream = new ClassPathResource("reportPdfConfig/modeReportConfig.jrxml").getInputStream(); // загрузка шаблона
            logger.debug("Pdf template loaded");

            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            logger.debug("Pdf template compiled");

            List<ModeReportDto> reportData = modeRepository.findAll().stream().map(mode -> new ModeReportDto(
                mode.getTitle(),
                mode.getType().toString(),
                getModeDevicesNames(mode)
            )).toList();
            logger.debug("Data taken from DB and change to ModeReportDto");

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData);
            logger.debug("Data about modes converted for writing to pdf");

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Report about modes of Smart Home System");
            parameters.put("generatedBy", SecurityContextHolder.getContext().getAuthentication().getName());
            logger.debug("Pdf parametrs entered");

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, parameters, dataSource);
            
            logger.info("Modes data report successfully created");
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
        catch (Exception e) {
            logger.warn("Mode report creating error: {}", e.getMessage(), e);
            throw new RuntimeException("Error to create report about modes", e);
        }
    }
}
