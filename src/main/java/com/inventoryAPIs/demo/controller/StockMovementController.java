package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.StockMovementFilterDTO;
import com.inventoryAPIs.demo.dto.StockMovementRequestDTO;
import com.inventoryAPIs.demo.dto.StockMovementResponseDTO;
import com.inventoryAPIs.demo.dto.StockTransferRequestDTO;
import com.inventoryAPIs.demo.dto.StockTransferResponseDTO;
import com.inventoryAPIs.demo.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public StockTransferResponseDTO transfer(@Valid @RequestBody StockTransferRequestDTO request) {
        return stockMovementService.transfer(request);
    }

    @GetMapping
    public PageResponseDTO<StockMovementResponseDTO> search(
            @Valid @ModelAttribute StockMovementFilterDTO filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return stockMovementService.search(filter, pageable);
    }
}
