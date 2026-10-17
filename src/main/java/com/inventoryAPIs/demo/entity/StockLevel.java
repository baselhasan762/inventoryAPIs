package com.inventoryAPIs.demo.entity;

import com.inventoryAPIs.demo.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Current quantity of one product at one location. Changed only through stock movements,
 * so there are no setters: use {@link #increase(int)} and {@link #decrease(int)}.
 */
@Entity
@Table(name = "stock_level",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "location_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"product", "location"})
public class StockLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false, updatable = false)
    private Location location;

    @Column(nullable = false)
    private int quantity;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private StockLevel(Product product, Location location) {
        this.product = product;
        this.location = location;
    }

    public static StockLevel empty(Product product, Location location) {
        return new StockLevel(product, location);
    }

    public void increase(int amount) {
        quantity += amount;
    }

    public void decrease(int amount) {
        if (amount > quantity) {
            throw new InsufficientStockException(product.getSku(), location.getCode(), amount, quantity);
        }
        quantity -= amount;
    }
}
