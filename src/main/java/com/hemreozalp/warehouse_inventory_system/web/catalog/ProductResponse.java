package com.hemreozalp.warehouse_inventory_system.web.catalog;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        CategorySummaryResponse category,
        SupplierSummaryResponse supplier,
        int minimumStockLevel,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
