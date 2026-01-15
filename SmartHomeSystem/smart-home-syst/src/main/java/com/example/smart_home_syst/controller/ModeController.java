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

import com.example.smart_home_syst.dto.ModeDto;
import com.example.smart_home_syst.enumerator.ModeType;
import com.example.smart_home_syst.model.Mode;
import com.example.smart_home_syst.service.ModeService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
public class ModeController {
    private final ModeService modeService;

    ModeController(ModeService modeService) {
        this.modeService = modeService;
    }

    @Operation(
    summary = "Get All modes",
    description = "Here we try to get list of all modes")
    @GetMapping("/modes")
    public List<Mode> getModes() {
        return modeService.getAll();
    }

    @Operation(
    summary = "Get mode with definite Id",
    description = "Specify the mode Id")
    @GetMapping("/modes/{id}")
    public ResponseEntity<Mode> getMode(@PathVariable Long id) {
        return ResponseEntity.ok().body(modeService.getById(id));
    }

    @Operation(
    summary = "Create new mode",
    description = "Fill in all the fields. Mode type at this moment: dinner, morning, study")
    @PostMapping("/modes")
    public ResponseEntity<Mode> addMode(@RequestBody @Valid ModeDto modeDto) {
       Mode newMode = modeService.create(modeDto);
       return  ResponseEntity.status(HttpStatus.CREATED).body(newMode);
    }

    @Operation(
    summary = "Update mode",
    description = "Specify the mode Id and fill in all the fields. Mode type at this moment: dinner, morning, study")
    @PutMapping("/modes/{id}")
    public ResponseEntity<Mode> editMode(@PathVariable Long id, @RequestBody @Valid ModeDto modeDto) {
        Mode updMode = modeService.update(id, modeDto);
        if(updMode != null) {
            return ResponseEntity.ok(updMode);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
    summary = "Delete mode",
    description = "Specify the mode Id")
    @DeleteMapping("/modes/{id}")
    public ResponseEntity <Void> deleteMode(@PathVariable Long id) {
        if (modeService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
    summary = "Get modes with filters",
    description = "Fill in all the fields and write in pageable sort field 1 field of mode for orderings")
    @GetMapping("/modes/filter")
    public ResponseEntity<Object> getByFilter(@RequestParam(required = false) String title, ModeType type, @PageableDefault(page=0, size=10, sort="title") Pageable pageable) {
        return ResponseEntity.ok(modeService.getByFilter(title, type, pageable));
    }
}
