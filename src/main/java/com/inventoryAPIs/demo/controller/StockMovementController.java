package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.StockMovementRequestDTO;
import com.inventoryAPIs.demo.dto.StockMovementResponseDTO;
import com.inventoryAPIs.demo.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponseDTO create(@Valid @RequestBody StockMovementRequestDTO request) {
        return stockMovementService.create(request);
    }

    @GetMapping
    public List<StockMovementResponseDTO> findAll() {
        return stockMovementService.findAll();
    }
}
