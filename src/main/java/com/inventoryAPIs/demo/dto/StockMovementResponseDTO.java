package com.inventoryAPIs.demo.dto;

import com.inventoryAPIs.demo.entity.MovementType;
import com.inventoryAPIs.demo.entity.StockMovement;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class StockMovementResponseDTO {

    private final Long id;
    private final Long productId;
    private final String productSku;
    private final String productName;
    private final Long locationId;
    private final String locationCode;
    private final MovementType type;
    private final int quantity;
    private final String reason;
    private final UUID transferId;
    private final Long createdById;
    private final String createdByName;
    private final LocalDateTime createdAt;

    public static StockMovementResponseDTO from(StockMovement movement) {
        return StockMovementResponseDTO.builder()
                .id(movement.getId())
                .productId(movement.getProduct().getId())
                .productSku(movement.getProduct().getSku())
                .productName(movement.getProduct().getName())
                .locationId(movement.getLocation().getId())
                .locationCode(movement.getLocation().getCode())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .reason(movement.getReason())
                .transferId(movement.getTransferId())
                .createdById(movement.getCreatedBy().getId())
                .createdByName(movement.getCreatedBy().getFullName())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
