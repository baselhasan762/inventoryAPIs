package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.StockMovementRequestDTO;
import com.inventoryAPIs.demo.dto.StockMovementResponseDTO;
import com.inventoryAPIs.demo.entity.Location;
import com.inventoryAPIs.demo.entity.Product;
import com.inventoryAPIs.demo.entity.StockMovement;
import com.inventoryAPIs.demo.exception.ResourceNotFoundException;
import com.inventoryAPIs.demo.repository.LocationRepository;
import com.inventoryAPIs.demo.repository.ProductRepository;
import com.inventoryAPIs.demo.repository.StockMovementRepository;
import com.inventoryAPIs.demo.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final CurrentUserProvider currentUserProvider;

    // TODO: update the matching StockLevel (and reject OUT beyond available stock) once StockLevel is implemented.
    public StockMovementResponseDTO create(StockMovementRequestDTO request) {
        StockMovement movement = StockMovement.builder()
                .product(findProduct(request.getProductId()))
                .location(findLocation(request.getLocationId()))
                .type(request.getType())
                .quantity(request.getQuantity())
                .reason(request.getReason())
                .createdBy(currentUserProvider.get())
                .build();

        return StockMovementResponseDTO.from(stockMovementRepository.save(movement));
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponseDTO> findAll() {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(StockMovementResponseDTO::from)
                .toList();
    }

    private Product findProduct(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Location findLocation(Long id) {
        return locationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
    }
}
