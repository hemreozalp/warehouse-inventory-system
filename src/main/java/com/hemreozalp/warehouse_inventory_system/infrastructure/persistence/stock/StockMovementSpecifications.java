package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.StockMovement;
import com.hemreozalp.warehouse_inventory_system.domain.stock.StockMovementType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class StockMovementSpecifications {

    private StockMovementSpecifications() {
    }

    public static Specification<StockMovement> stockIdEquals(UUID stockId) {
        return (root, query, criteriaBuilder) -> stockId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("stock").get("id"), stockId);
    }

    public static Specification<StockMovement> productIdEquals(UUID productId) {
        return (root, query, criteriaBuilder) -> productId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("product").get("id"), productId);
    }

    public static Specification<StockMovement> warehouseIdEquals(UUID warehouseId) {
        return (root, query, criteriaBuilder) -> warehouseId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("warehouse").get("id"), warehouseId);
    }

    public static Specification<StockMovement> typeEquals(StockMovementType type) {
        return (root, query, criteriaBuilder) -> type == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<StockMovement> createdAtGreaterThanOrEqualTo(Instant from) {
        return (root, query, criteriaBuilder) -> from == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<StockMovement> createdAtLessThanOrEqualTo(Instant to) {
        return (root, query, criteriaBuilder) -> to == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
