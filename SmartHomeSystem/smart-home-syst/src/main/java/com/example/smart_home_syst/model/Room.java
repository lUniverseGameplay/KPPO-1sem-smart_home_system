package com.example.smart_home_syst.model;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity

@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min=1, max=150)
    @Column(nullable = false, unique = true, length = 150)
    private String title;

    @NotBlank
    @Size(min=1, max=150)
    @Column(nullable = false, unique = true, length = 150)
    private String location;

    //private User manager;

    @JsonIgnore
    @OneToMany(mappedBy="room", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Device> devices;
    
    @JsonProperty("devices")
    public List<String> getDeviceTitles() {
        if (devices == null) {
            return List.of();
        }
        return devices.stream()
                .map(Device::getTitle)
                .collect(Collectors.toList());
    }

    private Integer capacity;
}
