package com.hemreozalp.warehouse_inventory_system.web.health;

import java.time.Instant;

public record HealthResponse(String status, Instant timestamp) {
}
