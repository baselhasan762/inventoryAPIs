package com.inventoryAPIs.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Body of both refresh and logout. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "refreshToken")
public class RefreshRequestDTO {

    @NotBlank
    private String refreshToken;
}
