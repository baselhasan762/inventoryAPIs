package com.inventoryAPIs.demo.dto;

import com.inventoryAPIs.demo.entity.MovementType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StockMovementRequestDTO {

    private static final Set<MovementType> SUPPORTED_TYPES = Set.of(MovementType.IN, MovementType.OUT);

    @NotNull
    private Long productId;

    @NotNull
    private Long locationId;

    @NotNull
    private MovementType type;

    /** Always positive; the type decides whether stock goes up or down. */
    @NotNull
    @Positive
    private Integer quantity;

    @Size(max = 255)
    private String reason;

    @AssertTrue(message = "only IN and OUT movements are supported")
    private boolean isSupportedType() {
        return type == null || SUPPORTED_TYPES.contains(type);
    }
}
