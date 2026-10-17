package com.inventoryAPIs.demo.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String sku, String locationCode, int requested, int available) {
        super("Insufficient stock for product %s at location %s: requested %d, available %d"
                .formatted(sku, locationCode, requested, available));
    }
}
