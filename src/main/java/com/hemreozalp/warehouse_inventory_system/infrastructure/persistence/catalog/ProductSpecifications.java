package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog;

import com.hemreozalp.warehouse_inventory_system.domain.catalog.Product;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> skuContains(String sku) {
        return (root, query, criteriaBuilder) -> {
            if (isBlank(sku)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), like(sku));
        };
    }

    public static Specification<Product> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (isBlank(name)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), like(name));
        };
    }

    public static Specification<Product> categoryIdEquals(UUID categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> supplierIdEquals(UUID supplierId) {
        return (root, query, criteriaBuilder) -> {
            if (supplierId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("supplier").get("id"), supplierId);
        };
    }

    public static Specification<Product> activeEquals(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String like(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
