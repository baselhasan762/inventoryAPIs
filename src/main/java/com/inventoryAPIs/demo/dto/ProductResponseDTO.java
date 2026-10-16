package com.inventoryAPIs.demo.dto;

import com.inventoryAPIs.demo.entity.Product;
import com.inventoryAPIs.demo.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductResponseDTO {

    private final Long id;
    private final String sku;
    private final String name;
    private final String description;
    private final Long categoryId;
    private final BigDecimal unitPrice;
    private final BigDecimal costPrice;
    private final String unitOfMeasure;
    private final int reorderLevel;
    private final Long addedById;
    private final String addedByName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ProductResponseDTO from(Product product) {
        User addedBy = product.getAddedBy();
        return ProductResponseDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategoryId())
                .unitPrice(product.getUnitPrice())
                .costPrice(product.getCostPrice())
                .unitOfMeasure(product.getUnitOfMeasure())
                .reorderLevel(product.getReorderLevel())
                .addedById(addedBy != null ? addedBy.getId() : null)
                .addedByName(addedBy != null ? addedBy.getFullName() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
