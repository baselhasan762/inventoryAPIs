package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.CategoryRequestDTO;
import com.inventoryAPIs.demo.dto.CategoryResponseDTO;
import com.inventoryAPIs.demo.dto.CategoryUpdateDTO;
import com.inventoryAPIs.demo.entity.Category;
import com.inventoryAPIs.demo.exception.DuplicateResourceException;
import com.inventoryAPIs.demo.exception.ResourceInUseException;
import com.inventoryAPIs.demo.exception.ResourceNotFoundException;
import com.inventoryAPIs.demo.repository.CategoryRepository;
import com.inventoryAPIs.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponseDTO create(CategoryRequestDTO request) {
        ensureNameAvailable(request.getName());

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return CategoryResponseDTO.from(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAllByActiveTrueOrderByName().stream()
                .map(CategoryResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long id) {
        return CategoryResponseDTO.from(findActive(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponseDTO update(Long id, CategoryUpdateDTO request) {
        Category category = findActive(id);

        if (request.getName() != null) {
            // A case-only change (e.g. "electronics" -> "Electronics") is a rename of the same name, not a clash.
            if (!request.getName().equalsIgnoreCase(category.getName())) {
                ensureNameAvailable(request.getName());
            }
            category.setName(request.getName());
        }
        Optional.ofNullable(request.getDescription()).ifPresent(category::setDescription);

        return CategoryResponseDTO.from(category);
    }

    /** Soft delete, refused while active products still belong to the category. */
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        Category category = findActive(id);

        if (productRepository.existsByCategoryIdAndActiveTrue(id)) {
            throw new ResourceInUseException("Category still has active products: " + category.getName());
        }
        category.setActive(false);
    }

    /** Also used by {@link ProductService} to resolve the category id sent in product requests. */
    @Transactional(readOnly = true)
    public Category findActive(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private void ensureNameAvailable(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category name already in use: " + name);
        }
    }
}
