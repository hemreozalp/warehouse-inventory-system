package com.hemreozalp.warehouse_inventory_system.web.stock;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateStockRequest(
        @NotNull
        UUID productId,

        @NotNull
        UUID warehouseId
) {
}
