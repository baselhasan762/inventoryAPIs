package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.AuthResponseDTO;
import com.inventoryAPIs.demo.dto.LoginRequestDTO;
import com.inventoryAPIs.demo.dto.RefreshRequestDTO;
import com.inventoryAPIs.demo.entity.User;
import com.inventoryAPIs.demo.exception.InvalidRefreshTokenException;
import com.inventoryAPIs.demo.exception.ResourceNotFoundException;
import com.inventoryAPIs.demo.repository.UserRepository;
import com.inventoryAPIs.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Deliberately not {@code @Transactional}: {@link RefreshTokenService#consume} must own its transaction,
 * otherwise a rollback here would undo the revoke-all done on refresh token reuse.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails user = (UserDetails) authentication.getPrincipal();
        User account = userRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + user.getUsername()));

        return issueTokens(user, account);
    }

    /** Rotates the refresh token: the old one is used up and a new access/refresh pair is issued. */
    public AuthResponseDTO refresh(RefreshRequestDTO request) {
        User account = refreshTokenService.consume(request.getRefreshToken());
        UserDetails user = userDetailsService.loadUserByUsername(account.getEmail());

        if (!user.isEnabled()) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        return issueTokens(user, account);
    }

    public void logout(RefreshRequestDTO request) {
        refreshTokenService.revoke(request.getRefreshToken());
    }

    private AuthResponseDTO issueTokens(UserDetails user, User account) {
        return AuthResponseDTO.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(refreshTokenService.create(account))
                .tokenType("Bearer")
                .build();
    }
}
