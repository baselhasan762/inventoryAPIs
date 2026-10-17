package com.inventoryAPIs.demo.dto;

import com.inventoryAPIs.demo.entity.StockLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StockLevelResponseDTO {

    private final Long id;
    private final Long productId;
    private final String productSku;
    private final String productName;
    private final Long locationId;
    private final String locationCode;
    private final int quantity;
    private final LocalDateTime updatedAt;

    public static StockLevelResponseDTO from(StockLevel stockLevel) {
        return StockLevelResponseDTO.builder()
                .id(stockLevel.getId())
                .productId(stockLevel.getProduct().getId())
                .productSku(stockLevel.getProduct().getSku())
                .productName(stockLevel.getProduct().getName())
                .locationId(stockLevel.getLocation().getId())
                .locationCode(stockLevel.getLocation().getCode())
                .quantity(stockLevel.getQuantity())
                .updatedAt(stockLevel.getUpdatedAt())
                .build();
    }
}
