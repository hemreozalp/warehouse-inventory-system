package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.Stock;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class StockSpecifications {

    private StockSpecifications() {
    }

    public static Specification<Stock> productIdEquals(UUID productId) {
        return (root, query, criteriaBuilder) -> {
            if (productId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("product").get("id"), productId);
        };
    }

    public static Specification<Stock> warehouseIdEquals(UUID warehouseId) {
        return (root, query, criteriaBuilder) -> {
            if (warehouseId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("warehouse").get("id"), warehouseId);
        };
    }
}
