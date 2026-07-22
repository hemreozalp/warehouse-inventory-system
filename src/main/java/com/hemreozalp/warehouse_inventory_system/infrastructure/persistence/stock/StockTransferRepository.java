package com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.StockTransfer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockTransferRepository extends JpaRepository<StockTransfer, UUID>, JpaSpecificationExecutor<StockTransfer> {
}
