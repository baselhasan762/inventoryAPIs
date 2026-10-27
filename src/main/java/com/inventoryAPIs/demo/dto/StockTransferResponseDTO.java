package com.inventoryAPIs.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/** The two movements a transfer creates, linked by {@code transferId}. */
@Getter
@Builder
public class StockTransferResponseDTO {

    private final UUID transferId;
    private final StockMovementResponseDTO outbound;
    private final StockMovementResponseDTO inbound;
}
