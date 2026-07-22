package com.hemreozalp.warehouse_inventory_system.web.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.StockTransferStatus;
import java.time.Instant;
import java.util.UUID;

public record StockTransferResponse(
        UUID id,
        UUID productId,
        StockResponse sourceStock,
        StockResponse targetStock,
        WarehouseSummaryResponse sourceWarehouse,
        WarehouseSummaryResponse targetWarehouse,
        int quantity,
        StockTransferStatus status,
        StockMovementResponse sourceMovement,
        StockMovementResponse targetMovement,
        String reason,
        Instant createdAt
) {
}
