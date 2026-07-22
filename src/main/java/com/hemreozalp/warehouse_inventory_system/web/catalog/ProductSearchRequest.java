package com.hemreozalp.warehouse_inventory_system.web.catalog;

import java.util.UUID;

public record ProductSearchRequest(
        String sku,
        String name,
        UUID categoryId,
        UUID supplierId,
        Boolean active
) {
}
