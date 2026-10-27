package com.inventoryAPIs.demo.repository;

import com.inventoryAPIs.demo.dto.StockMovementFilterDTO;
import com.inventoryAPIs.demo.entity.StockMovement;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** Builds the WHERE clause of the movement history query from whichever filters were sent. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StockMovementSpecifications {

    public static Specification<StockMovement> matching(StockMovementFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }
            if (StringUtils.hasText(filter.getProductName())) {
                String pattern = "%" + filter.getProductName().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("product").get("name")), pattern));
            }
            if (filter.getLocationId() != null) {
                predicates.add(cb.equal(root.get("location").get("id"), filter.getLocationId()));
            }
            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }
            if (filter.getTransferId() != null) {
                predicates.add(cb.equal(root.get("transferId"), filter.getTransferId()));
            }
            if (filter.getCreatedById() != null) {
                predicates.add(cb.equal(root.get("createdBy").get("id"), filter.getCreatedById()));
            }
            if (filter.getFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFrom()));
            }
            if (filter.getTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getTo()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
