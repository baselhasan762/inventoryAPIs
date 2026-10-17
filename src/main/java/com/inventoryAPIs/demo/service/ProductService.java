package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.LowStockProductDTO;
import com.inventoryAPIs.demo.dto.ProductRequestDTO;
import com.inventoryAPIs.demo.dto.ProductResponseDTO;
import com.inventoryAPIs.demo.dto.ProductUpdateDTO;
import com.inventoryAPIs.demo.entity.Category;
import com.inventoryAPIs.demo.entity.Product;
import com.inventoryAPIs.demo.exception.DuplicateResourceException;
import com.inventoryAPIs.demo.exception.ResourceNotFoundException;
import com.inventoryAPIs.demo.repository.ProductRepository;
import com.inventoryAPIs.demo.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;
    private final CategoryService categoryService;

    public ProductResponseDTO create(ProductRequestDTO request) {
        ensureSkuAvailable(request.getSku());

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .category(resolveCategory(request.getCategoryId()))
                .unitPrice(request.getUnitPrice())
                .costPrice(request.getCostPrice())
                .unitOfMeasure(request.getUnitOfMeasure())
                .reorderLevel(Optional.ofNullable(request.getReorderLevel()).orElse(0))
                .addedBy(currentUserProvider.get())
                .build();

        return ProductResponseDTO.from(productRepository.save(product));
    }

    /** Lists all active products, or only those in the given category when {@code categoryId} is set. */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll(Long categoryId) {
        List<Product> products = categoryId == null
                ? productRepository.findAllByActiveTrue()
                : productRepository.findAllByActiveTrueAndCategoryId(categoryId);

        return products.stream()
                .map(ProductResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LowStockProductDTO> findLowStock() {
        return productRepository.findLowStockProducts();
    }

    @PreAuthorize("hasRole('ADMIN') or @productSecurity.isOwner(#id, authentication)")
    public ProductResponseDTO update(Long id, ProductUpdateDTO request) {
        Product product = findProduct(id);

        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            ensureSkuAvailable(request.getSku());
            product.setSku(request.getSku());
        }
        Optional.ofNullable(request.getName()).ifPresent(product::setName);
        Optional.ofNullable(request.getDescription()).ifPresent(product::setDescription);
        Optional.ofNullable(request.getCategoryId()).map(categoryService::findActive).ifPresent(product::setCategory);
        Optional.ofNullable(request.getUnitPrice()).ifPresent(product::setUnitPrice);
        Optional.ofNullable(request.getCostPrice()).ifPresent(product::setCostPrice);
        Optional.ofNullable(request.getUnitOfMeasure()).ifPresent(product::setUnitOfMeasure);
        Optional.ofNullable(request.getReorderLevel()).ifPresent(product::setReorderLevel);

        return ProductResponseDTO.from(product);
    }

    /** Soft delete: stock levels and movements keep a foreign key to the product, so it is deactivated instead. */
    @PreAuthorize("hasRole('ADMIN') or @productSecurity.isOwner(#id, authentication)")
    public void delete(Long id) {
        findProduct(id).setActive(false);
    }

    private Product findProduct(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    /** Category is optional on create; a given id must point to an active category. */
    private Category resolveCategory(Long categoryId) {
        return categoryId == null ? null : categoryService.findActive(categoryId);
    }

    private void ensureSkuAvailable(String sku) {
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("SKU already in use: " + sku);
        }
    }
}
