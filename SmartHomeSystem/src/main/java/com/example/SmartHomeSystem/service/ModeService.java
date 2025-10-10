package com.example.SmartHomeSystem.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.SmartHomeSystem.enums.ModeType;
import com.example.SmartHomeSystem.model.Mode;
import com.example.SmartHomeSystem.repository.ModeRepository;

@Service
public class ModeService {
    private final ModeRepository modeRepository;

    public ModeService(ModeRepository modeRepository) {
        this.modeRepository = modeRepository;
    }

    private final List<Mode> modes = new ArrayList<>();

    public List<Mode> getAll() {
        return modeRepository.findAll();

    }

    public List<Mode> getAllByType(ModeType type) {
        return modeRepository.findAllByType(type);
    }

    public Mode create(Mode mode) {
        return modeRepository.save(mode);
    }

    public Mode getById(Long id) {
        for (Mode mode : modes) {
            if (mode.getId().equals(id)) {
                return modeRepository.findById(id).orElse(null);
            }
        }
        return null;
    }

    public Mode update(Long id, Mode mode) {
        return modeRepository.findById(id).map(existingMode -> {
            existingMode.setType(mode.getType());
            //existingMode.setRole(mode.getRole()); - как сделать?
            return modeRepository.save(existingMode);
        }).orElse(null);
    }

    public boolean deleteById(Long id) {
        if (modeRepository.existsById(id)) {
            modeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
