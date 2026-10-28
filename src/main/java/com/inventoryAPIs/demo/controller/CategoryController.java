package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.CategoryRequestDTO;
import com.inventoryAPIs.demo.dto.CategoryResponseDTO;
import com.inventoryAPIs.demo.dto.CategoryUpdateDTO;
import com.inventoryAPIs.demo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product categories. Any logged-in user can read them; only admins can change them")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a category", description = "Admin only. Names must be unique.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO create(@Valid @RequestBody CategoryRequestDTO request) {
        return categoryService.create(request);
    }

    @Operation(summary = "List categories")
    @GetMapping
    public List<CategoryResponseDTO> findAll() {
        return categoryService.findAll();
    }

    @Operation(summary = "Get a category")
    @GetMapping("/{id}")
    public CategoryResponseDTO findById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @Operation(summary = "Update a category", description = "Admin only. Only the fields you send are changed.")
    @PatchMapping("/{id}")
    public CategoryResponseDTO update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateDTO request) {
        return categoryService.update(id, request);
    }

    @Operation(summary = "Deactivate a category", description = "Admin only. Fails with 409 while the category still has active products.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
