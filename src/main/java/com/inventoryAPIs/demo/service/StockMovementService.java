package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.StockMovementRequestDTO;
import com.inventoryAPIs.demo.dto.StockMovementResponseDTO;
import com.inventoryAPIs.demo.entity.Location;
import com.inventoryAPIs.demo.entity.MovementType;
import com.inventoryAPIs.demo.entity.Product;
import com.inventoryAPIs.demo.entity.StockLevel;
import com.inventoryAPIs.demo.entity.StockMovement;
import com.inventoryAPIs.demo.exception.ResourceNotFoundException;
import com.inventoryAPIs.demo.repository.LocationRepository;
import com.inventoryAPIs.demo.repository.ProductRepository;
import com.inventoryAPIs.demo.repository.StockLevelRepository;
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
    private final StockLevelRepository stockLevelRepository;
    private final CurrentUserProvider currentUserProvider;

    /** Records the movement and applies it to the stock level in one transaction, so the two never disagree. */
    public StockMovementResponseDTO create(StockMovementRequestDTO request) {
        Product product = findProduct(request.getProductId());
        Location location = findLocation(request.getLocationId());

        applyToStockLevel(product, location, request.getType(), request.getQuantity());


        StockMovement movement = StockMovement.builder()
                .product(product)
                .location(location)
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

    private void applyToStockLevel(Product product, Location location, MovementType type, int quantity) {
        StockLevel stockLevel = stockLevelRepository.findForUpdate(product.getId(), location.getId())
                .orElseGet(() -> StockLevel.empty(product, location));

        switch (type) {
            case IN -> stockLevel.increase(quantity);
            case OUT -> stockLevel.decrease(quantity);
            default -> throw new IllegalArgumentException("Unsupported movement type: " + type);
        }

        stockLevelRepository.save(stockLevel);
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
