package com.hemreozalp.warehouse_inventory_system.web.stock;

import java.util.UUID;

public record WarehouseSummaryResponse(UUID id, String code, String name, boolean active) {
}
