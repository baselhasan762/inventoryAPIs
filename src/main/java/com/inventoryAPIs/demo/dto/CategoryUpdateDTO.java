package com.inventoryAPIs.demo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Partial category update; null fields are left unchanged. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CategoryUpdateDTO {

    private static final String NOT_BLANK = ".*\\S.*";

    @Pattern(regexp = NOT_BLANK, message = "must not be blank")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;
}
