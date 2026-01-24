package com.example.smart_home_syst.dto;

import java.util.List;


public class ModeReportDto {
    private String title;
    private String type;
    private String devicesNames;

    public ModeReportDto(String title, String type, String devicesNames) {
        this.title = title;
        this.type = type;
        this.devicesNames = devicesNames;
    }
    
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getDevices() { return devicesNames; }
}
