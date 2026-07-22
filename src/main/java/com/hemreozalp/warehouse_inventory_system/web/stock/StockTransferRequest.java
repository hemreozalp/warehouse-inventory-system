package com.hemreozalp.warehouse_inventory_system.web.stock;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StockTransferRequest(
        @NotNull
        UUID sourceStockId,

        @NotNull
        UUID targetWarehouseId,

        @Positive
        int quantity,

        @Size(max = 500)
        String reason
) {
}
