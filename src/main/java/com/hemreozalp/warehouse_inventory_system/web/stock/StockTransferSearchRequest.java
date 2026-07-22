package com.hemreozalp.warehouse_inventory_system.web.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.StockTransferStatus;
import java.time.Instant;
import java.util.UUID;

public record StockTransferSearchRequest(
        UUID productId,
        UUID sourceWarehouseId,
        UUID targetWarehouseId,
        StockTransferStatus status,
        Instant createdFrom,
        Instant createdTo
) {
}
