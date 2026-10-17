package com.inventoryAPIs.demo.repository;

import com.inventoryAPIs.demo.entity.StockLevel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockLevel s WHERE s.product.id = :productId AND s.location.id = :locationId")
    Optional<StockLevel> findForUpdate(@Param("productId") Long productId, @Param("locationId") Long locationId);

    @EntityGraph(attributePaths = {"product", "location"})
    @Query("""
            SELECT s FROM StockLevel s
            WHERE s.product.active = true
              AND s.location.active = true
              AND (:productId IS NULL OR s.product.id = :productId)
              AND (:locationId IS NULL OR s.location.id = :locationId)
            ORDER BY s.product.sku, s.location.code
            """)
    List<StockLevel> search(@Param("productId") Long productId, @Param("locationId") Long locationId);
}
