package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.LowStockProductDTO;
import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.ProductRequestDTO;
import com.inventoryAPIs.demo.dto.ProductResponseDTO;
import com.inventoryAPIs.demo.dto.ProductUpdateDTO;
import com.inventoryAPIs.demo.entity.Category;
import com.inventoryAPIs.demo.entity.Product;
import com.inventoryAPIs.demo.exception.DuplicateResourceException;
import com.inventoryAPIs.demo.exception.ResourceInUseException;
import com.inventoryAPIs.demo.exception.ResourceNotFoundException;
import com.inventoryAPIs.demo.repository.ProductRepository;
import com.inventoryAPIs.demo.repository.StockLevelRepository;
import com.inventoryAPIs.demo.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final StockLevelRepository stockLevelRepository;
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

    /**
     * Lists active products. Both filters are optional: {@code categoryId} limits it to one category and
     * {@code search} matches part of the name, ignoring case. A blank search counts as no search.
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponseDTO> findAll(Long categoryId, String search, Pageable pageable) {
        String term = StringUtils.hasText(search) ? search.trim() : null;
        Page<Product> products = productRepository.search(categoryId, term, pageable);

        return PageResponseDTO.from(products, ProductResponseDTO::from);
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

    /**
     * Soft delete: stock levels and movements keep a foreign key to the product, so it is deactivated instead.
     * Refused while any location still holds it, which would otherwise vanish from every stock query.
     */
    @PreAuthorize("hasRole('ADMIN') or @productSecurity.isOwner(#id, authentication)")
    public void delete(Long id) {
        Product product = findProduct(id);

        if (stockLevelRepository.existsByProductIdAndQuantityGreaterThan(id, 0)) {
            throw new ResourceInUseException("Product still has stock: " + product.getSku());
        }
        product.setActive(false);
    }

    /**
     * Undoes a soft delete; reactivating an already active product is a no-op.
     * A category deleted in the meantime is cleared, so the product never points to an inactive category.
     */
    @PreAuthorize("hasRole('ADMIN') or @productSecurity.isOwner(#id, authentication)")
    public ProductResponseDTO reactivate(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getCategory() != null && !product.getCategory().isActive()) {
            product.setCategory(null);
        }
        product.setActive(true);

        return ProductResponseDTO.from(product);
    }

    public Product findProduct(Long id) {
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
