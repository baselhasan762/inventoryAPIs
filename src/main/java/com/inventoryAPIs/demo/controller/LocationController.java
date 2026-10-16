package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.LocationRequestDTO;
import com.inventoryAPIs.demo.dto.LocationResponseDTO;
import com.inventoryAPIs.demo.dto.LocationUpdateDTO;
import com.inventoryAPIs.demo.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponseDTO create(@Valid @RequestBody LocationRequestDTO request) {
        return locationService.create(request);
    }

    @GetMapping
    public List<LocationResponseDTO> findAll() {
        return locationService.findAll();
    }

    @GetMapping("/{id}")
    public LocationResponseDTO findById(@PathVariable Long id) {
        return locationService.findById(id);
    }

    @PatchMapping("/{id}")
    public LocationResponseDTO update(@PathVariable Long id, @Valid @RequestBody LocationUpdateDTO request) {
        return locationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }
}
