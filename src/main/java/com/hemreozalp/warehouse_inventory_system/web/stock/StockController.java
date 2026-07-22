package com.hemreozalp.warehouse_inventory_system.web.stock;

import com.hemreozalp.warehouse_inventory_system.application.stock.StockService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    StockResponse create(@Valid @RequestBody CreateStockRequest request) {
        return stockService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    StockResponse get(@PathVariable UUID id) {
        return stockService.get(id);
    }

    @GetMapping("/by-product-warehouse")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    StockResponse getByProductAndWarehouse(
            @RequestParam UUID productId,
            @RequestParam UUID warehouseId
    ) {
        return stockService.getByProductAndWarehouse(productId, warehouseId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    Page<StockResponse> search(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID warehouseId,
            Pageable pageable
    ) {
        return stockService.search(new StockSearchRequest(productId, warehouseId), pageable);
    }

    @PatchMapping("/stock-in")
    @PreAuthorize("hasRole('ADMIN')")
    StockOperationResponse stockIn(@Valid @RequestBody StockChangeRequest request) {
        return stockService.stockIn(request);
    }

    @PatchMapping("/stock-out")
    @PreAuthorize("hasRole('ADMIN')")
    StockOperationResponse stockOut(@Valid @RequestBody StockChangeRequest request) {
        return stockService.stockOut(request);
    }

    @PatchMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    StockOperationResponse adjust(@Valid @RequestBody StockAdjustmentRequest request) {
        return stockService.adjust(request);
    }
}
