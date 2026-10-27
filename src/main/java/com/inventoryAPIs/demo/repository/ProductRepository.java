package com.inventoryAPIs.demo.repository;

import com.inventoryAPIs.demo.dto.LowStockProductDTO;
import com.inventoryAPIs.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

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

    /**
     * Active products with two optional filters (null means "any"): a category, and part of the name.
     * Fetches {@code addedBy} and {@code category} in the same query to avoid extra selects per product.
     */
    @EntityGraph(attributePaths = {"addedBy", "category"})
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Product> search(@Param("categoryId") Long categoryId, @Param("search") String search, Pageable pageable);
}

