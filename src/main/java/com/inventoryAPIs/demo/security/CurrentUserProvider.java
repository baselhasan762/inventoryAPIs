package com.inventoryAPIs.demo.security;

import com.inventoryAPIs.demo.entity.User;
import com.inventoryAPIs.demo.exception.ResourceNotFoundException;
import com.inventoryAPIs.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Loads the {@link User} entity of the currently authenticated request. */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User get() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
