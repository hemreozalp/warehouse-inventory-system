package com.hemreozalp.warehouse_inventory_system.web.stock;

import com.hemreozalp.warehouse_inventory_system.application.stock.StockReportService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class StockReportController {

    private final StockReportService reportService;

    public StockReportController(StockReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    Page<LowStockResponse> lowStock(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID warehouseId,
            Pageable pageable
    ) {
        return reportService.lowStock(productId, warehouseId, pageable);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    DashboardMetricsResponse dashboard() {
        return reportService.dashboardMetrics();
    }
}
