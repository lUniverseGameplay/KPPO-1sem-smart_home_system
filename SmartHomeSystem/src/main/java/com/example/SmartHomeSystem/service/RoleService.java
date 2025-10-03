package com.example.SmartHomeSystem.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.SmartHomeSystem.model.Role;
import com.example.SmartHomeSystem.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    private final List<Role> roles = new ArrayList<>();

    public List<Role> getAll() {
        return roleRepository.findAll();

    }

    public List<Role> getAllByTitle(String title) {
        return roleRepository.findAllByTitle(title);
    }

    public Role create(Role role) {
        return roleRepository.save(role);
    }

    public Role getById(Long id) {
        for (Role role : roles) {
            if (role.getId().equals(id)) {
                return roleRepository.findById(id).orElse(null);
            }
        }
        return null;
    }

    public Role update(Long id, Role role) {
        return roleRepository.findById(id).map(existingRole -> {
            existingRole.setTitle(role.getTitle());
            //как сделать?
            return roleRepository.save(existingRole);
        }).orElse(null);
    }

    public boolean deleteById(Long id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
