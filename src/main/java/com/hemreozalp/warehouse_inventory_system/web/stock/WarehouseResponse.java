package com.hemreozalp.warehouse_inventory_system.web.stock;

import java.time.Instant;
import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        String code,
        String name,
        String address,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
