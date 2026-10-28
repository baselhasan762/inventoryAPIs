package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.StockMovementFilterDTO;
import com.inventoryAPIs.demo.dto.StockMovementRequestDTO;
import com.inventoryAPIs.demo.dto.StockMovementResponseDTO;
import com.inventoryAPIs.demo.dto.StockTransferRequestDTO;
import com.inventoryAPIs.demo.dto.StockTransferResponseDTO;
import com.inventoryAPIs.demo.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Stock movements", description = "Stock coming in, going out and moving between locations. Every change to a stock level is recorded here")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @Operation(summary = "Record stock in or out", description = "Type IN or OUT; transfers have their own endpoint. Updates the stock level in the same transaction. OUT fails with 409 if there isn't enough stock.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponseDTO create(@Valid @RequestBody StockMovementRequestDTO request) {
        return stockMovementService.create(request);
    }

    @Operation(summary = "Transfer stock between locations", description = "Recorded as a TRANSFER_OUT and a TRANSFER_IN sharing one transfer id. Both happen or neither does. Fails with 409 if the source doesn't have enough stock.")
    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public StockTransferResponseDTO transfer(@Valid @RequestBody StockTransferRequestDTO request) {
        return stockMovementService.transfer(request);
    }

    @Operation(summary = "Search stock movements", description = "Paged, newest first. Every filter is optional; 'from' and 'to' limit the creation date.")
    @GetMapping
    public PageResponseDTO<StockMovementResponseDTO> search(
            @Valid @ParameterObject
            @ModelAttribute StockMovementFilterDTO filter,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return stockMovementService.search(filter, pageable);
    }
}
