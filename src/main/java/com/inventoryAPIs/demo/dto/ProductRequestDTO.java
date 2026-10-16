package com.inventoryAPIs.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductRequestDTO {

    @NotBlank
    @Size(max = 50)
    private String sku;

    @NotBlank
    @Size(max = 150)
    private String name;

    private String description;

    private Long categoryId;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal costPrice;

    @NotBlank
    @Size(max = 20)
    private String unitOfMeasure;

    /** Optional; defaults to 0. */
    @PositiveOrZero
    private Integer reorderLevel;
}
