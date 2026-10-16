package com.inventoryAPIs.demo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Partial location update; null fields are left unchanged. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocationUpdateDTO {

    private static final String NOT_BLANK = ".*\\S.*";

    @Pattern(regexp = NOT_BLANK, message = "must not be blank")
    @Size(max = 100)
    private String name;

    @Pattern(regexp = NOT_BLANK, message = "must not be blank")
    @Size(max = 30)
    private String code;

    @Size(max = 255)
    private String address;
}
