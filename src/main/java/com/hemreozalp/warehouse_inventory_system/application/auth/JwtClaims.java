package com.hemreozalp.warehouse_inventory_system.application.auth;

import com.hemreozalp.warehouse_inventory_system.domain.user.Role;
import java.time.Instant;

public record JwtClaims(String subject, Role role, Instant expiresAt) {
}
