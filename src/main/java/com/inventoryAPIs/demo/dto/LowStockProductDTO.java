package com.inventoryAPIs.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** A product whose total stock across all locations is at or below its reorder level. */
@Getter
@AllArgsConstructor
public class LowStockProductDTO {

    private final Long id;
    private final String sku;
    private final String name;
    private final int reorderLevel;
    private final long totalQuantity;
}
