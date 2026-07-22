package com.hemreozalp.warehouse_inventory_system.web.stock;

public record DashboardMetricsResponse(
        long activeProductCount,
        long activeWarehouseCount,
        long stockItemCount,
        long totalQuantityOnHand,
        long lowStockItemCount,
        long movementCount
) {
}
