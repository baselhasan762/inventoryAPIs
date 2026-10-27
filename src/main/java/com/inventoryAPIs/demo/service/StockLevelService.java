package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.StockLevelResponseDTO;
import com.inventoryAPIs.demo.repository.StockLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-only: stock levels change only through {@link StockMovementService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockLevelService {

    private final StockLevelRepository stockLevelRepository;

    public PageResponseDTO<StockLevelResponseDTO> search(Long productId, Long locationId, Pageable pageable) {
        return PageResponseDTO.from(stockLevelRepository.search(productId, locationId, pageable),
                StockLevelResponseDTO::from);
    }
}
