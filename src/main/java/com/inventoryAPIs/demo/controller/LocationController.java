package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.LocationRequestDTO;
import com.inventoryAPIs.demo.dto.LocationResponseDTO;
import com.inventoryAPIs.demo.dto.LocationUpdateDTO;
import com.inventoryAPIs.demo.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Warehouses and other places that hold stock. Only admins can change them")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Create a location", description = "Admin only. Codes must be unique.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponseDTO create(@Valid @RequestBody LocationRequestDTO request) {
        return locationService.create(request);
    }

    @Operation(summary = "List locations")
    @GetMapping
    public List<LocationResponseDTO> findAll() {
        return locationService.findAll();
    }

    @Operation(summary = "Get a location")
    @GetMapping("/{id}")
    public LocationResponseDTO findById(@PathVariable Long id) {
        return locationService.findById(id);
    }

    @Operation(summary = "Update a location", description = "Admin only. Only the fields you send are changed.")
    @PatchMapping("/{id}")
    public LocationResponseDTO update(@PathVariable Long id, @Valid @RequestBody LocationUpdateDTO request) {
        return locationService.update(id, request);
    }

    @Operation(summary = "Reactivate a location", description = "Admin only.")
    @PatchMapping("/{id}/reactivate")
    public LocationResponseDTO reactivate(@PathVariable Long id) {
        return locationService.reactivate(id);
    }

    @Operation(summary = "Deactivate a location", description = "Admin only. Fails with 409 while the location still holds stock. Can be undone with /reactivate.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }
}
