package com.inventoryAPIs.demo.repository;

import com.inventoryAPIs.demo.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findAllByActiveTrue();

    Optional<Location> findByIdAndActiveTrue(Long id);

    /** Checks all locations, including soft-deleted ones, since they still hold their code in the unique column. */
    boolean existsByCode(String code);
}
