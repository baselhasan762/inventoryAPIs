package com.inventoryAPIs.demo.repository;

import com.inventoryAPIs.demo.entity.StockMovement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /** Fetches the lazy associations in the same query, since the response DTO reads all three. */
    @EntityGraph(attributePaths = {"product", "location", "createdBy"})
    List<StockMovement> findAllByOrderByCreatedAtDesc();
}
