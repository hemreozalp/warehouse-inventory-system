package com.hemreozalp.warehouse_inventory_system.web.stock;

public record StockOperationResponse(
        StockResponse stock,
        StockMovementResponse movement
) {
}
