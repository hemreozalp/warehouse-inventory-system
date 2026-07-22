package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog;

import com.hemreozalp.warehouse_inventory_system.domain.catalog.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, UUID id);

    long countByActiveTrue();
}
