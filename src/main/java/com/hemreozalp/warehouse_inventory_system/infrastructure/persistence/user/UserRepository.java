package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.user;

import com.hemreozalp.warehouse_inventory_system.domain.user.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
