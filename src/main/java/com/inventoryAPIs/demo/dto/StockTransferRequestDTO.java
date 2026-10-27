package com.inventoryAPIs.demo.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StockTransferRequestDTO {

    @NotNull
    private Long productId;

    @NotNull
    private Long fromLocationId;

    @NotNull
    private Long toLocationId;

    @NotNull
    @Positive
    private Integer quantity;

    @Size(max = 255)
    private String reason;

    @AssertTrue(message = "source and destination locations must be different")
    private boolean isDifferentLocations() {
        return fromLocationId == null || !fromLocationId.equals(toLocationId);
    }
}
