package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.LocationRequestDTO;
import com.inventoryAPIs.demo.dto.LocationResponseDTO;
import com.inventoryAPIs.demo.dto.LocationUpdateDTO;
import com.inventoryAPIs.demo.entity.Location;
import com.inventoryAPIs.demo.exception.DuplicateResourceException;
import com.inventoryAPIs.demo.exception.ResourceNotFoundException;
import com.inventoryAPIs.demo.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public LocationResponseDTO create(LocationRequestDTO request) {
        ensureCodeAvailable(request.getCode());

        Location location = Location.builder()
                .name(request.getName())
                .code(request.getCode())
                .address(request.getAddress())
                .build();

        return LocationResponseDTO.from(locationRepository.save(location));
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> findAll() {
        return locationRepository.findAllByActiveTrue().stream()
                .map(LocationResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LocationResponseDTO findById(Long id) {
        return LocationResponseDTO.from(findLocation(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public LocationResponseDTO update(Long id, LocationUpdateDTO request) {
        Location location = findLocation(id);

        if (request.getCode() != null && !request.getCode().equals(location.getCode())) {
            ensureCodeAvailable(request.getCode());
            location.setCode(request.getCode());
        }
        Optional.ofNullable(request.getName()).ifPresent(location::setName);
        Optional.ofNullable(request.getAddress()).ifPresent(location::setAddress);

        return LocationResponseDTO.from(location);
    }

    /** Soft delete: stock levels and movements keep a foreign key to the location, so it is deactivated instead. */
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        findLocation(id).setActive(false);
    }

    private Location findLocation(Long id) {
        return locationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
    }

    private void ensureCodeAvailable(String code) {
        if (locationRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Location code already in use: " + code);
        }
    }
}
