package com.hemreozalp.warehouse_inventory_system.web.catalog;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 120)
        String contactName,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 50)
        String phone,

        @NotNull
        Boolean active
) {
}
