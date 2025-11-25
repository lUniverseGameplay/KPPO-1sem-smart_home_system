package com.example.smart_home_syst.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final List<Mode> modes = new ArrayList<>();

    @Transactional(readOnly = true)
    @Cacheable(value="modes", key="#root.methodName")
    public List<Mode> getAll() {
        return modeRepository.findAll();
    }

    public List<Mode> getAllByTitle(String title) {
        return modeRepository.findAllByTitle(title);
    }

    @Transactional(readOnly = true)
    @Cacheable(value="mode", key="#id")
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
            existingMode.setTitle(mode.getTitle());
            return modeRepository.save(existingMode);
        }).orElseThrow(null);
    }

    @Caching(evict = {
        @CacheEvict(value="modes", allEntries=true),
        @CacheEvict(value="mode", key="#id")
    })
    @Transactional
    public boolean deleteById(Long id) {
        if (modeRepository.existsById(id)) {
            modeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    @CacheEvict(value="modes", allEntries=true)
    public Mode create (Mode mode) {
        return modeRepository.save(mode);
    }

    public Page<Mode> getByFilter(String title, ModeType type, Pageable pageable) {
        return modeRepository.findAll(ModeSpecifications.filter(title, type), pageable);
    }
}
