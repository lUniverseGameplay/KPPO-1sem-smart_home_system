package com.example.smart_home_syst.dto;

public class RoomReportDto {
    private String title;
    private String location;
    private Integer capacity;
    private String managerName;
    private String devicesNames;

    public RoomReportDto(String title, String location, Integer capacity, String managerName, String devicesNames) {
        this.title = title;
        this.location = location;
        this.capacity = capacity;
        this.managerName = managerName;
        this.devicesNames = devicesNames;
    }
    
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public Integer getCapacity() { return capacity; }
    public String getManagerName() { return managerName; }
    public String getDevicesNames() { return devicesNames; }
}
