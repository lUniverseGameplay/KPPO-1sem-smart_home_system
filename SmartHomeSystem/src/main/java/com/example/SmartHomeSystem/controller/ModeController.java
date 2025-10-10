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

import com.example.SmartHomeSystem.model.Mode;
import com.example.SmartHomeSystem.service.ModeService;

import jakarta.validation.Valid;

@RestController
public class ModeController {
    private final ModeService modeService;

    public ModeController(ModeService modeService) {
        this.modeService = modeService;
    }

    @GetMapping("/modes")
    public List<Mode> getModes() {
        return modeService.getAll();
    }

    @GetMapping("/modes/{id}")
    public ResponseEntity<Mode> getMode(@PathVariable Long id) {
        return ResponseEntity.ok().body(modeService.getById(id));
    }
    
    @PutMapping("/modes/{id}")
    public ResponseEntity<Mode> edit(@PathVariable Long id, @RequestBody Mode mode) {
        Mode updated = modeService.update(id, mode);
        if(updated != null){
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/modes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if(modeService.deleteById(id)){
            return ResponseEntity.noContent().build();
        }
         return ResponseEntity.ok().build();
    }

    @PostMapping("/modes")
    public ResponseEntity<Mode> addMode(@RequestBody @Valid Mode mode) {
        Mode newMode = modeService.create(mode);
        return ResponseEntity.status(HttpStatus.CREATED).body(newMode);
    }
}
