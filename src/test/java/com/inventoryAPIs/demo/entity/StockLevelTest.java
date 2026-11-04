package com.inventoryAPIs.demo.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.inventoryAPIs.demo.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

class StockLevelTest {

    private final Product product = Product.builder().sku("SKU-1").build();
    private final Location location = Location.builder().code("WH-1").build();

    @Test
    void increaseAddsToQuantity() {
        StockLevel stockLevel = StockLevel.empty(product, location);

        stockLevel.increase(5);

        assertThat(stockLevel.getQuantity()).isEqualTo(5);

    }

    @Test
    void decreaseSubtractsFromQuantity() {
        StockLevel stockLevel = StockLevel.empty(product, location);
        stockLevel.increase(10);

        stockLevel.decrease(3);

        assertThat(stockLevel.getQuantity()).isEqualTo(7);
    }

    @Test
    void decreaseCanReachZero() {
        StockLevel stockLevel = StockLevel.empty(product, location);
        stockLevel.increase(5);

        stockLevel.decrease(5);
        assertThat(stockLevel.getQuantity()).isEqualTo(0);
    }

    @Test
    void decreaseMoreThanAvailableThrowsAndKeepsQuantity() {
        StockLevel stockLevel = StockLevel.empty(product, location);
        stockLevel.increase(5);

        assertThatThrownBy(() -> stockLevel.decrease(6))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(stockLevel.getQuantity()).isEqualTo(5);
    }


}
