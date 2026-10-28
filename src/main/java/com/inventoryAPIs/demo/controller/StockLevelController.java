package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.StockLevelResponseDTO;
import com.inventoryAPIs.demo.service.StockLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-levels")
@RequiredArgsConstructor
@Tag(name = "Stock levels", description = "Current quantity of each product at each location")
public class StockLevelController {

    private final StockLevelService stockLevelService;

    @Operation(summary = "Search stock levels", description = "Paged. Filter by product, location, both or neither.")
    @GetMapping
    public PageResponseDTO<StockLevelResponseDTO> search(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long locationId,
            @ParameterObject @PageableDefault(size = 20, sort = {"product.sku", "location.code"}) Pageable pageable) {
        return stockLevelService.search(productId, locationId, pageable);
    }
}
