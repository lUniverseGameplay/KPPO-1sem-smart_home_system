package com.example.SmartHomeSystem.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartHomeSystem.model.Role;
import com.example.SmartHomeSystem.service.RoleService;

import jakarta.validation.Valid;



@RestController
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public List<Role> getRoles() {
        return roleService.getAll();
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<Role> getRole(@PathVariable Long id) {
        return ResponseEntity.ok().body(roleService.getById(id));
    }
    
    @PutMapping("/roles/{id}")
    public ResponseEntity<Role> edit(@PathVariable Long id, @RequestBody Role role) {
        Role updated = roleService.update(id, role);
        if(updated != null){
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if(roleService.deleteById(id)){
            return ResponseEntity.noContent().build();
        }
         return ResponseEntity.ok().build();
    }

    @PostMapping("/roles")
    public ResponseEntity<Role> addRole(@RequestBody @Valid Role role) {
        Role newRole = roleService.create(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRole);
    }
}
