package com.inventoryAPIs.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDTO {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
}
