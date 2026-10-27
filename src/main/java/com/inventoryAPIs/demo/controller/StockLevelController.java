package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.StockLevelResponseDTO;
import com.inventoryAPIs.demo.service.StockLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-levels")
@RequiredArgsConstructor
public class StockLevelController {

    private final StockLevelService stockLevelService;

    @GetMapping
    public PageResponseDTO<StockLevelResponseDTO> search(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long locationId,
            @PageableDefault(size = 20, sort = {"product.sku", "location.code"}) Pageable pageable) {
        return stockLevelService.search(productId, locationId, pageable);
    }
}
