package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.LowStockProductDTO;
import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.ProductRequestDTO;
import com.inventoryAPIs.demo.dto.ProductResponseDTO;
import com.inventoryAPIs.demo.dto.ProductUpdateDTO;
import com.inventoryAPIs.demo.entity.Product;
import com.inventoryAPIs.demo.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO create(@Valid @RequestBody ProductRequestDTO request) {
        return productService.create(request);
    }

    @GetMapping
    public PageResponseDTO<ProductResponseDTO> findAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return productService.findAll(categoryId, search, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponseDTO findById(@PathVariable Long id){
       Product p = productService.findProduct(id);
       return ProductResponseDTO.from(p);
    }

    @GetMapping("/low-stock")
    public List<LowStockProductDTO> findLowStock() {
        return productService.findLowStock();
    }

    @PatchMapping("/{id}")
    public ProductResponseDTO update(@PathVariable Long id, @Valid @RequestBody ProductUpdateDTO request) {
        return productService.update(id, request);
    }

    @PatchMapping("/{id}/reactivate")
    public ProductResponseDTO reactivate(@PathVariable Long id) {
        return productService.reactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
