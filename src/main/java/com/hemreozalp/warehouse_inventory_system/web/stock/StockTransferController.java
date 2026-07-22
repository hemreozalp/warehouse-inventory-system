package com.hemreozalp.warehouse_inventory_system.web.stock;

import com.hemreozalp.warehouse_inventory_system.application.stock.StockTransferService;
import com.hemreozalp.warehouse_inventory_system.domain.stock.StockTransferStatus;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stock-transfers")
public class StockTransferController {

    private final StockTransferService transferService;

    public StockTransferController(StockTransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    StockTransferResponse transfer(
            @Valid @RequestBody StockTransferRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return transferService.transfer(request, idempotencyKey);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    StockTransferResponse get(@PathVariable UUID id) {
        return transferService.get(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    Page<StockTransferResponse> search(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID sourceWarehouseId,
            @RequestParam(required = false) UUID targetWarehouseId,
            @RequestParam(required = false) StockTransferStatus status,
            @RequestParam(required = false) Instant createdFrom,
            @RequestParam(required = false) Instant createdTo,
            Pageable pageable
    ) {
        return transferService.search(
                new StockTransferSearchRequest(
                        productId,
                        sourceWarehouseId,
                        targetWarehouseId,
                        status,
                        createdFrom,
                        createdTo),
                pageable);
    }
}
