package com.hemreozalp.warehouse_inventory_system.web.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.StockMovementType;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID stockId,
        UUID productId,
        UUID warehouseId,
        StockMovementType type,
        int quantity,
        int quantityBefore,
        int quantityAfter,
        String reason,
        Instant createdAt
) {
}
