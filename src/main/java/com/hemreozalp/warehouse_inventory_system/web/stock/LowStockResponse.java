package com.hemreozalp.warehouse_inventory_system.web.stock;

import java.util.UUID;

public record LowStockResponse(
        UUID stockId,
        UUID productId,
        String sku,
        String productName,
        UUID warehouseId,
        String warehouseCode,
        int quantity,
        int minimumStockLevel,
        int shortageQuantity
) {
}
