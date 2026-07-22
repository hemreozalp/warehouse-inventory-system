package com.hemreozalp.warehouse_inventory_system.web.stock;

import com.hemreozalp.warehouse_inventory_system.web.catalog.ProductResponse;
import java.time.Instant;
import java.util.UUID;

public record StockResponse(
        UUID id,
        ProductResponse product,
        WarehouseSummaryResponse warehouse,
        int quantity,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
}
