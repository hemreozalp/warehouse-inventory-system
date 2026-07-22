package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog;

import com.hemreozalp.warehouse_inventory_system.domain.catalog.Supplier;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
}
