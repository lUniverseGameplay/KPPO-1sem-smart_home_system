package com.example.smart_home_syst.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.smart_home_syst.model.Mode;

@Repository
public interface ModeRepository extends JpaRepository<Mode, Long>, JpaSpecificationExecutor<Mode> {
    List<Mode> findAllByTitle(String title);
    List<Mode> findByTitleStartingWithIgnoreCase(String title);
}
