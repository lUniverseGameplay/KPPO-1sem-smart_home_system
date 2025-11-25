package com.example.smart_home_syst.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_home_syst.enumerator.ModeType;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.service.ModeService;

import jakarta.validation.Valid;

@RestController
public class ModeController {
    private final ModeService modeService;

    ModeController(ModeService modeService) {
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

    @PostMapping("/modes")
    public ResponseEntity<Mode> addMode(@RequestBody @Valid Mode mode) {
       Mode newMode = modeService.create(mode);
       return  ResponseEntity.status(HttpStatus.CREATED).body(newMode);
    }

    @PutMapping("/modes/{id}")
    public ResponseEntity<Mode> editMode(@PathVariable Long id, @RequestBody @Valid Mode mode) {
        Mode updMode = modeService.update(id, mode);
        if(updMode != null) {
            return ResponseEntity.ok(updMode);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modes/{id}")
    public ResponseEntity <Void> deleteMode(@PathVariable Long id) {
        if (modeService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/modes/filter")
    public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String title, ModeType type, @PageableDefault(page=0, size=10, sort="title") Pageable pageable) {
        return ResponseEntity.ok(modeService.getByFilter(title, type, pageable));
    }
}
