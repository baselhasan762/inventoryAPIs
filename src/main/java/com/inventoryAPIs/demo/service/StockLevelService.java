package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.StockLevelResponseDTO;
import com.inventoryAPIs.demo.repository.StockLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Read-only: stock levels change only through {@link StockMovementService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockLevelService {

    private final StockLevelRepository stockLevelRepository;

    public List<StockLevelResponseDTO> search(Long productId, Long locationId) {
        return stockLevelRepository.search(productId, locationId).stream()
                .map(StockLevelResponseDTO::from)
                .toList();
    }
}
