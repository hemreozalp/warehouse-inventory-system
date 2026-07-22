package com.hemreozalp.warehouse_inventory_system.application.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.Stock;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog.ProductRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockMovementRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.WarehouseRepository;
import com.hemreozalp.warehouse_inventory_system.web.stock.DashboardMetricsResponse;
import com.hemreozalp.warehouse_inventory_system.web.stock.LowStockResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockReportService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository movementRepository;

    public StockReportService(
            StockRepository stockRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            StockMovementRepository movementRepository
    ) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public Page<LowStockResponse> lowStock(UUID productId, UUID warehouseId, Pageable pageable) {
        return stockRepository.findLowStockItems(productId, warehouseId, pageable)
                .map(this::toLowStockResponse);
    }

    @Transactional(readOnly = true)
    public DashboardMetricsResponse dashboardMetrics() {
        return new DashboardMetricsResponse(
                productRepository.countByActiveTrue(),
                warehouseRepository.countByActiveTrue(),
                stockRepository.count(),
                stockRepository.sumQuantityOnHand(),
                stockRepository.countLowStockItems(),
                movementRepository.count());
    }

    private LowStockResponse toLowStockResponse(Stock stock) {
        int minimumStockLevel = stock.getProduct().getMinimumStockLevel();
        int shortageQuantity = Math.max(minimumStockLevel - stock.getQuantity(), 0);

        return new LowStockResponse(
                stock.getId(),
                stock.getProduct().getId(),
                stock.getProduct().getSku(),
                stock.getProduct().getName(),
                stock.getWarehouse().getId(),
                stock.getWarehouse().getCode(),
                stock.getQuantity(),
                minimumStockLevel,
                shortageQuantity);
    }
}
