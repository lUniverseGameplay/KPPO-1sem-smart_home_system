package com.example.smart_home_syst.model;

import java.util.List;

import com.example.smart_home_syst.enumerator.ModeType;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Data

@Entity
public class Mode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min=1, max=150)
    @Column(nullable = false, unique = true, length = 150)
    private String title;
    
    private ModeType type;

    @JsonIgnore
    @OneToMany(mappedBy="mode", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Device> devices;
    
    /*@JsonProperty("devices")
    public List<String> getDeviceTitles() {
        if (devices == null) {
            return List.of();
        }
        return devices.stream()
                .map(Device::getTitle)
                .collect(Collectors.toList());
    }*/
}
