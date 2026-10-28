package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.LowStockProductDTO;
import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.ProductRequestDTO;
import com.inventoryAPIs.demo.dto.ProductResponseDTO;
import com.inventoryAPIs.demo.dto.ProductUpdateDTO;
import com.inventoryAPIs.demo.entity.Product;
import com.inventoryAPIs.demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalogue, search and low-stock alerts")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a product", description = "SKUs must be unique. The creator becomes the product's owner.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO create(@Valid @RequestBody ProductRequestDTO request) {
        return productService.create(request);
    }

    @Operation(summary = "Search products", description = "Paged. 'search' matches part of the name, ignoring case; both filters are optional.")
    @GetMapping
    public PageResponseDTO<ProductResponseDTO> findAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return productService.findAll(categoryId, search, pageable);
    }

    @Operation(summary = "Get a product")
    @GetMapping("/{id}")
    public ProductResponseDTO findById(@PathVariable Long id){
       Product p = productService.findProduct(id);
       return ProductResponseDTO.from(p);
    }

    @Operation(summary = "List low-stock products", description = "Products whose total quantity across all locations is at or below their reorder level.")
    @GetMapping("/low-stock")
    public List<LowStockProductDTO> findLowStock() {
        return productService.findLowStock();
    }

    @Operation(summary = "Update a product", description = "Admin or the product's owner. Only the fields you send are changed.")
    @PatchMapping("/{id}")
    public ProductResponseDTO update(@PathVariable Long id, @Valid @RequestBody ProductUpdateDTO request) {
        return productService.update(id, request);
    }

    @Operation(summary = "Reactivate a product", description = "Admin or the product's owner.")
    @PatchMapping("/{id}/reactivate")
    public ProductResponseDTO reactivate(@PathVariable Long id) {
        return productService.reactivate(id);
    }

    @Operation(summary = "Deactivate a product", description = "Admin or the product's owner. Fails with 409 while the product still has stock. Can be undone with /reactivate.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
