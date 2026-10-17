package com.inventoryAPIs.demo.repository;

import com.inventoryAPIs.demo.dto.LowStockProductDTO;
import com.inventoryAPIs.demo.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Fetches {@code addedBy} and {@code category} in the same query to avoid extra selects per product. */
    @EntityGraph(attributePaths = {"addedBy", "category"})
    List<Product> findAllByActiveTrue();

    @EntityGraph(attributePaths = {"addedBy", "category"})
    List<Product> findAllByActiveTrueAndCategoryId(Long categoryId);

    boolean existsByCategoryIdAndActiveTrue(Long categoryId);

    Optional<Product> findByIdAndActiveTrue(Long id);

    /** Checks all products, including soft-deleted ones, since they still hold their SKU in the unique column. */
    boolean existsBySku(String sku);

    /** Products with no stock rows count as 0, so they show up as low stock too. Lowest stock first. */
    @Query("""
            SELECT new com.inventoryAPIs.demo.dto.LowStockProductDTO(
                p.id, p.sku, p.name, p.reorderLevel, COALESCE(SUM(s.quantity), 0L))
            FROM Product p
            LEFT JOIN StockLevel s ON s.product = p
            WHERE p.active = true
            GROUP BY p.id, p.sku, p.name, p.reorderLevel
            HAVING COALESCE(SUM(s.quantity), 0L) <= p.reorderLevel
            ORDER BY COALESCE(SUM(s.quantity), 0L) ASC
            """)
    List<LowStockProductDTO> findLowStockProducts();
}
