package com.inventoryAPIs.demo.repository;

import com.inventoryAPIs.demo.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long>,
        JpaSpecificationExecutor<StockMovement> {

    /** Redeclared only to fetch the relations the response needs in the same query. */
    @Override
    @EntityGraph(attributePaths = {"product", "location", "createdBy"})
    Page<StockMovement> findAll(Specification<StockMovement> spec, Pageable pageable);
}
