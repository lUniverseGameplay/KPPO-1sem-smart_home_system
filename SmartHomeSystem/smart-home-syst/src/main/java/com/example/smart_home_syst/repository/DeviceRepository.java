package com.example.smart_home_syst.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.smart_home_syst.model.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
    List<Device> findAllByTitle(String title);
    List<Device> findByTitleStartingWithIgnoreCase(String title);
}
