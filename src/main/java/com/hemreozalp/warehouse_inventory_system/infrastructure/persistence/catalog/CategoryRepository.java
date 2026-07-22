package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog;

import com.hemreozalp.warehouse_inventory_system.domain.catalog.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
