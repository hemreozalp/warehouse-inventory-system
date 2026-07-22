package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.StockTransfer;
import com.hemreozalp.warehouse_inventory_system.domain.stock.StockTransferStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class StockTransferSpecifications {

    private StockTransferSpecifications() {
    }

    public static Specification<StockTransfer> productIdEquals(UUID productId) {
        return (root, query, criteriaBuilder) -> productId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("product").get("id"), productId);
    }

    public static Specification<StockTransfer> sourceWarehouseIdEquals(UUID warehouseId) {
        return (root, query, criteriaBuilder) -> warehouseId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("sourceWarehouse").get("id"), warehouseId);
    }

    public static Specification<StockTransfer> targetWarehouseIdEquals(UUID warehouseId) {
        return (root, query, criteriaBuilder) -> warehouseId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("targetWarehouse").get("id"), warehouseId);
    }

    public static Specification<StockTransfer> statusEquals(StockTransferStatus status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<StockTransfer> createdAtGreaterThanOrEqualTo(Instant from) {
        return (root, query, criteriaBuilder) -> from == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<StockTransfer> createdAtLessThanOrEqualTo(Instant to) {
        return (root, query, criteriaBuilder) -> to == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
