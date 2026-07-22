package com.hemreozalp.warehouse_inventory_system.web.auth;

import com.hemreozalp.warehouse_inventory_system.domain.user.Role;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        String fullName,
        Role role
) {
}
