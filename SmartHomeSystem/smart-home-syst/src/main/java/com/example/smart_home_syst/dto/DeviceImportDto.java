package com.example.smart_home_syst.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;

@Data
@JacksonXmlRootElement(localName = "devices")
public class DeviceImportDto {
     @JacksonXmlProperty(localName = "exportDate", isAttribute = true)
    private String exportDate;
    
    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private String version;
    
    @JacksonXmlProperty(localName = "totalCount", isAttribute = true)
    private int totalCount;
    
    @JacksonXmlProperty(localName = "system")
    private SystemInfo system;
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "device")
    private List<DeviceDto> devices;
    
    @Data
    public static class SystemInfo {
        @JacksonXmlProperty(localName = "name")
        private String name;
        
        @JacksonXmlProperty(localName = "exportedBy")
        private String exportedBy;
        
        @JacksonXmlProperty(localName = "format")
        private String format;
    }
}