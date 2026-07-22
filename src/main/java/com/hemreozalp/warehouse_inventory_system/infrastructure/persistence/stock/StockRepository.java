package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.Stock;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Stock, UUID>, JpaSpecificationExecutor<Stock> {

    boolean existsByProductIdAndWarehouseId(UUID productId, UUID warehouseId);

    Optional<Stock> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);

    @Query("select coalesce(sum(stock.quantity), 0) from Stock stock")
    long sumQuantityOnHand();

    @Query("""
            select count(stock)
            from Stock stock
            join stock.product product
            where product.active = true
              and stock.quantity <= product.minimumStockLevel
            """)
    long countLowStockItems();

    @Query(
            value = """
                    select stock
                    from Stock stock
                    join fetch stock.product product
                    join fetch stock.warehouse warehouse
                    where product.active = true
                      and warehouse.active = true
                      and stock.quantity <= product.minimumStockLevel
                      and (:productId is null or product.id = :productId)
                      and (:warehouseId is null or warehouse.id = :warehouseId)
                    """,
            countQuery = """
                    select count(stock)
                    from Stock stock
                    join stock.product product
                    join stock.warehouse warehouse
                    where product.active = true
                      and warehouse.active = true
                      and stock.quantity <= product.minimumStockLevel
                      and (:productId is null or product.id = :productId)
                      and (:warehouseId is null or warehouse.id = :warehouseId)
                    """)
    Page<Stock> findLowStockItems(
            @Param("productId") UUID productId,
            @Param("warehouseId") UUID warehouseId,
            Pageable pageable);
}
