package com.example.smart_home_syst.dto;

import com.example.smart_home_syst.enumerator.DeviceType;

public class DeviceReportDto {
    private String title;
    private String type; // Убедитесь, что тип String, а не DeviceType
    private Double power;
    private boolean active;
    private String roomTitle;
    private String modeTitle;

    public DeviceReportDto(String title, String type, Double power, boolean active, 
                          String roomTitle, String modeTitle) {
        this.title = title;
        this.type = type;
        this.power = power;
        this.active = active;
        this.roomTitle = roomTitle;
        this.modeTitle = modeTitle;
    }
    
    // Геттеры (ОБЯЗАТЕЛЬНО нужны!)
    public String getTitle() { return title; }
    public String getType() { return type; }
    public Double getPower() { return power; }
    public boolean getActive() { return active; } // или isActive()
    public String getRoomTitle() { return roomTitle; }
    public String getModeTitle() { return modeTitle; }
}
