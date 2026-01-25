package com.example.smart_home_syst.fileSettings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import com.example.smart_home_syst.dto.RoomDto;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;

// Для более красивого вывода xml файла
@Data
@JacksonXmlRootElement(localName = "SmartHomeSystem-Rooms")
public class RoomsExportWrapper {
    @JacksonXmlProperty(localName = "exportDate", isAttribute = true)
    private String exportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    
    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private String version = "1.0";
    
    @JacksonXmlProperty(localName = "totalCount", isAttribute = true)
    private int totalCount;
    
    @JacksonXmlProperty(localName = "system")
    private SystemInfo system = new SystemInfo();
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "room")
    private List<RoomDto> rooms;
    
    @Data
    public static class SystemInfo {
        @JacksonXmlProperty(localName = "name")
        private String name = "Smart Home System";
        
        @JacksonXmlProperty(localName = "exportedBy")
        private String exportedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        
        @JacksonXmlProperty(localName = "format")
        private String format = "XML";
    }
}
