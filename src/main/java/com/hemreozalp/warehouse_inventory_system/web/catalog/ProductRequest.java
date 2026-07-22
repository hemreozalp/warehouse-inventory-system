package com.hemreozalp.warehouse_inventory_system.web.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ProductRequest(
        @NotBlank
        @Size(max = 80)
        String sku,

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull
        UUID categoryId,

        UUID supplierId,

        @Min(0)
        int minimumStockLevel,

        @NotNull
        Boolean active
) {
}
