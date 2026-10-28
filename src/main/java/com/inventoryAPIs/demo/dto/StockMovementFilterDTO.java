package com.inventoryAPIs.demo.dto;

import com.inventoryAPIs.demo.entity.MovementType;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Optional query-string filters for the movement history; every null field is ignored.
 * Dates are ISO date-times, e.g. {@code from=2026-10-01T00:00:00}.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class StockMovementFilterDTO {

    private Long productId;

    /** Partial, case-insensitive match, e.g. {@code laptop} finds "Dell Latitude 5440 Laptop". */
    private String productName;

    private Long locationId;
    private MovementType type;
    private UUID transferId;
    private Long createdById;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    @AssertTrue(message = "'from' must not be after 'to'")
    private boolean isValidDateRange() {
        return from == null || to == null || !from.isAfter(to);
    }
}
