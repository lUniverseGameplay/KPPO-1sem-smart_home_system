package com.example.smart_home_syst.fileSettings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

import com.example.smart_home_syst.dto.DeviceDto;
import com.example.smart_home_syst.dto.ModeDto;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;


@Data
@JacksonXmlRootElement(localName = "SmartHomeSystem-Modes")
public class ModesExportWrapper {
    
    @JacksonXmlProperty(localName = "exportDate", isAttribute = true)
    private String exportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    
    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private String version = "1.0";
    
    @JacksonXmlProperty(localName = "totalCount", isAttribute = true)
    private int totalCount;
    
    @JacksonXmlProperty(localName = "system")
    private SystemInfo system = new SystemInfo();
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "mode")
    private List<ModeDto> modes;
    
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
