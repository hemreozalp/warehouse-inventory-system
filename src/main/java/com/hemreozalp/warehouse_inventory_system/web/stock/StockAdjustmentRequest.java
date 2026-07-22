package com.hemreozalp.warehouse_inventory_system.web.stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StockAdjustmentRequest(
        @NotNull
        UUID stockId,

        @Min(0)
        int quantity,

        @Size(max = 500)
        String reason
) {
}
