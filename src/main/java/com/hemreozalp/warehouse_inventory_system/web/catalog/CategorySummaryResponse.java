package com.hemreozalp.warehouse_inventory_system.web.catalog;

import java.util.UUID;

public record CategorySummaryResponse(UUID id, String name, boolean active) {
}
