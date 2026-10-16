package com.inventoryAPIs.demo.security;

import com.inventoryAPIs.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Ownership checks used from {@code @PreAuthorize} expressions as {@code @productSecurity}. */
@Component("productSecurity")
@RequiredArgsConstructor
public class ProductSecurity {

    private final ProductRepository productRepository;

    public boolean isOwner(Long productId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return productRepository.findById(productId)
                .map(product -> product.getAddedBy() != null
                        && product.getAddedBy().getEmail().equals(authentication.getName() ))
                .orElse(false);
    }
}
