package com.hemreozalp.warehouse_inventory_system.web.catalog;

import java.time.Instant;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String name,
        String contactName,
        String email,
        String phone,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
