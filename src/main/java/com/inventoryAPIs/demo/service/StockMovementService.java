package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.StockMovementFilterDTO;
import com.inventoryAPIs.demo.dto.StockMovementRequestDTO;
import com.inventoryAPIs.demo.dto.StockMovementResponseDTO;
import com.inventoryAPIs.demo.dto.StockTransferRequestDTO;
import com.inventoryAPIs.demo.dto.StockTransferResponseDTO;
import com.inventoryAPIs.demo.entity.Location;
import com.inventoryAPIs.demo.entity.MovementType;
import com.inventoryAPIs.demo.entity.Product;
import com.inventoryAPIs.demo.entity.StockLevel;
import com.inventoryAPIs.demo.entity.StockMovement;
import com.inventoryAPIs.demo.exception.BusinessRuleException;
import com.inventoryAPIs.demo.exception.ResourceNotFoundException;
import com.inventoryAPIs.demo.repository.LocationRepository;
import com.inventoryAPIs.demo.repository.ProductRepository;
import com.inventoryAPIs.demo.repository.StockLevelRepository;
import com.inventoryAPIs.demo.repository.StockMovementRepository;
import com.inventoryAPIs.demo.repository.StockMovementSpecifications;
import com.inventoryAPIs.demo.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
        if(request.getType() == MovementType.TRANSFER_IN || request.getType() == MovementType.TRANSFER_OUT){
            throw new BusinessRuleException("Use POST /api/stock-movements/transfers for transfers");
        }
        Product product = findProduct(request.getProductId());
        Location location = findLocation(request.getLocationId());

        StockLevel stockLevel = lockStockLevel(product, location);
        apply(stockLevel, request.getType(), request.getQuantity());
        stockLevelRepository.save(stockLevel);

        StockMovement movement = recordMovement(product, location, request.getType(),
                request.getQuantity(), request.getReason(), null);

        return StockMovementResponseDTO.from(movement);
    }

    /**
     * Moves stock between two locations as a TRANSFER_OUT / TRANSFER_IN pair sharing one transfer id.
     * Both sides commit together or not at all, so stock is never lost in between.
     */
    public StockTransferResponseDTO transfer(StockTransferRequestDTO request) {
        Product product = findProduct(request.getProductId());
        Location source = findLocation(request.getFromLocationId());
        Location destination = findLocation(request.getToLocationId());
        int quantity = request.getQuantity();

        // Always lock the lower location id first, so two opposite transfers (A->B and B->A) can't deadlock.
        StockLevel sourceStock;
        StockLevel destinationStock;
        if (source.getId() < destination.getId()) {
            sourceStock = lockStockLevel(product, source);
            destinationStock = lockStockLevel(product, destination);
        } else {
            destinationStock = lockStockLevel(product, destination);
            sourceStock = lockStockLevel(product, source);
        }

        apply(sourceStock, MovementType.TRANSFER_OUT, quantity);
        apply(destinationStock, MovementType.TRANSFER_IN, quantity);
        stockLevelRepository.saveAll(List.of(sourceStock, destinationStock));

        UUID transferId = UUID.randomUUID();
        StockMovement outbound = recordMovement(product, source, MovementType.TRANSFER_OUT,
                quantity, request.getReason(), transferId);
        StockMovement inbound = recordMovement(product, destination, MovementType.TRANSFER_IN,
                quantity, request.getReason(), transferId);

        return StockTransferResponseDTO.builder()
                .transferId(transferId)
                .outbound(StockMovementResponseDTO.from(outbound))
                .inbound(StockMovementResponseDTO.from(inbound))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<StockMovementResponseDTO> search(StockMovementFilterDTO filter, Pageable pageable) {
        return PageResponseDTO.from(stockMovementRepository.findAll(StockMovementSpecifications.matching(filter), pageable),
                StockMovementResponseDTO::from);
    }

    /** Locks the existing row, or starts an empty one if this product has never been stocked at the location. */
    private StockLevel lockStockLevel(Product product, Location location) {
        return stockLevelRepository.findForUpdate(product.getId(), location.getId())
                .orElseGet(() -> StockLevel.empty(product, location));
    }

    /** The movement type decides the direction; the quantity itself is always positive. */
    private void apply(StockLevel stockLevel, MovementType type, int quantity) {
        switch (type) {
            case IN, TRANSFER_IN -> stockLevel.increase(quantity);
            case OUT, TRANSFER_OUT -> stockLevel.decrease(quantity);
            default -> throw new IllegalArgumentException("Unsupported movement type: " + type);
        }
    }

    private StockMovement recordMovement(Product product, Location location, MovementType type,
                                         int quantity, String reason, UUID transferId) {
        StockMovement movement = StockMovement.builder()
                .product(product)
                .location(location)
                .type(type)
                .quantity(quantity)
                .reason(reason)
                .transferId(transferId)
                .createdBy(currentUserProvider.get())
                .build();

        return stockMovementRepository.save(movement);
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
