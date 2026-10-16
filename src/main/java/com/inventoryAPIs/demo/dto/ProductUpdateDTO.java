package com.inventoryAPIs.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/** Partial product update; null fields are left unchanged. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductUpdateDTO {

    private static final String NOT_BLANK = ".*\\S.*";

    @Pattern(regexp = NOT_BLANK, message = "must not be blank")
    @Size(max = 50)
    private String sku;

    @Pattern(regexp = NOT_BLANK, message = "must not be blank")
    @Size(max = 150)
    private String name;

    private String description;

    private Long categoryId;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal unitPrice;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal costPrice;

    @Pattern(regexp = NOT_BLANK, message = "must not be blank")
    @Size(max = 20)
    private String unitOfMeasure;

    @PositiveOrZero
    private Integer reorderLevel;
}
