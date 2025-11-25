package com.example.smart_home_syst.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.smart_home_syst.enumerator.ModeType;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.repository.ModeRepository;
import com.example.smart_home_syst.specifications.ModeSpecifications;

@Service
public class ModeService {
    private final ModeRepository modeRepository;

    public ModeService(ModeRepository modeRepository) {
        this.modeRepository = modeRepository;
    }

    /* отсюда дописать */


    public Page<Mode> getByFilter(String title, ModeType type, Pageable pageable) {
        return modeRepository.findAll(ModeSpecifications.filter(title, type), pageable);
    }
}
