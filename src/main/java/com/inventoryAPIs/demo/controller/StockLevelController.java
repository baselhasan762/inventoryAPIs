package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.StockLevelResponseDTO;
import com.inventoryAPIs.demo.service.StockLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock-levels")
@RequiredArgsConstructor
public class StockLevelController {

    private final StockLevelService stockLevelService;

    @GetMapping
    public List<StockLevelResponseDTO> search(@RequestParam(required = false) Long productId,
                                              @RequestParam(required = false) Long locationId) {
        return stockLevelService.search(productId, locationId);
    }
}
