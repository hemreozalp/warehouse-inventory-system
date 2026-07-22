package com.hemreozalp.warehouse_inventory_system.web.catalog;

import java.util.UUID;

public record SupplierSummaryResponse(UUID id, String name, boolean active) {
}
