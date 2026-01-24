package com.example.smart_home_syst.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;


@Data
@JacksonXmlRootElement(localName = "devices")
public class ModeListImportDto {
    @JacksonXmlProperty(localName = "exportDate", isAttribute = true)
    private String exportDate;
    
    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private String version;
    
    @JacksonXmlProperty(localName = "totalCount", isAttribute = true)
    private int totalCount;
    
    @JacksonXmlProperty(localName = "system")
    private SystemInfo system;
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "mode")
    private List<ModeDto> modes;
    
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
