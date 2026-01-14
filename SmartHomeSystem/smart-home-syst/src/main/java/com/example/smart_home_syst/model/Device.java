package com.example.smart_home_syst.model;

import com.example.smart_home_syst.enumerator.DeviceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Version
    @JsonIgnore
    private Integer version = 0;

    @NotBlank
    @Size(min=1, max=150)
    @Column(nullable = false, unique = true, length = 150)
    private String title;

    @JsonIgnore
    @ManyToOne
    private Room room;
    
    @JsonIgnore
    @ManyToOne
    //@JoinColumn(name = "mode_id")
    private Mode mode;
    
    private DeviceType type;

    private Double power;
    private boolean active;

    @JsonProperty("roomId")
    public Long getRoomId() {
        return room != null ? room.getId() : null;
    }

    @JsonProperty("modeId")
    public Long getModeId() {
        return mode != null ? mode.getId() : null;
    }
}
