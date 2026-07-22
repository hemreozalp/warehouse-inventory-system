package com.hemreozalp.warehouse_inventory_system.web.stock;

import java.util.UUID;

public record StockSearchRequest(UUID productId, UUID warehouseId) {
}
