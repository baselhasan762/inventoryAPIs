package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.AuthResponseDTO;
import com.inventoryAPIs.demo.dto.LoginRequestDTO;
import com.inventoryAPIs.demo.dto.RefreshRequestDTO;
import com.inventoryAPIs.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "Authentication", description = "Log in, refresh and log out. Access tokens last 15 minutes, refresh tokens 7 days")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Log in", description = "Returns a 15-minute access token and a 7-day refresh token. Paste the access token into Authorize.")
    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @Operation(summary = "Get new tokens", description = "Exchanges a refresh token for a new access token and a new refresh token. The old refresh token stops working; reusing it logs out every session of that user.")
    @PostMapping("/refresh")
    public AuthResponseDTO refresh(@Valid @RequestBody RefreshRequestDTO request) {
        return authService.refresh(request);
    }

    @Operation(summary = "Log out", description = "Revokes the refresh token. Always answers 204, even for an unknown token.")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequestDTO request) {
        authService.logout(request);
    }
}
