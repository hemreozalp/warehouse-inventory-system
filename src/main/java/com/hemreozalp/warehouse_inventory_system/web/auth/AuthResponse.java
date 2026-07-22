package com.hemreozalp.warehouse_inventory_system.web.auth;

import com.hemreozalp.warehouse_inventory_system.domain.user.Role;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UUID userId,
        String email,
        String fullName,
        Role role
) {
}
