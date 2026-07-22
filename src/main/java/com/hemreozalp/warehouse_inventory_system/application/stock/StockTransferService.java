package com.hemreozalp.warehouse_inventory_system.application.stock;

import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.common.exception.DomainException;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Stock;
import com.hemreozalp.warehouse_inventory_system.domain.stock.StockMovement;
import com.hemreozalp.warehouse_inventory_system.domain.stock.StockMovementType;
import com.hemreozalp.warehouse_inventory_system.domain.stock.StockTransfer;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Warehouse;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockMovementRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockTransferRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockTransferSpecifications;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockTransferRequest;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockTransferResponse;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockTransferSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockTransferService {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final StockTransferRepository transferRepository;
    private final StockService stockService;
    private final WarehouseService warehouseService;
    private final StockTransferMapper transferMapper;

    public StockTransferService(
            StockRepository stockRepository,
            StockMovementRepository movementRepository,
            StockTransferRepository transferRepository,
            StockService stockService,
            WarehouseService warehouseService,
            StockTransferMapper transferMapper
    ) {
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
        this.transferRepository = transferRepository;
        this.stockService = stockService;
        this.warehouseService = warehouseService;
        this.transferMapper = transferMapper;
    }

    @Transactional
    public StockTransferResponse transfer(StockTransferRequest request) {
        return transfer(request, null);
    }

    @Transactional
    public StockTransferResponse transfer(StockTransferRequest request, String idempotencyKey) {
        String normalizedIdempotencyKey = normalizeIdempotencyKey(idempotencyKey);
        if (normalizedIdempotencyKey != null) {
            java.util.Optional<StockTransfer> existingTransfer =
                    transferRepository.findByIdempotencyKey(normalizedIdempotencyKey);
            if (existingTransfer.isPresent()) {
                return transferMapper.toResponse(existingTransfer.get());
            }
        }

        Stock sourceStock = stockService.findStock(request.sourceStockId());
        Warehouse targetWarehouse = warehouseService.findWarehouse(request.targetWarehouseId());

        ensureTransferIsAllowed(sourceStock, targetWarehouse);

        Stock targetStock = stockRepository
                .findByProductIdAndWarehouseId(sourceStock.getProduct().getId(), targetWarehouse.getId())
                .orElseGet(() -> stockRepository.save(new Stock(sourceStock.getProduct(), targetWarehouse)));

        int sourceBefore;
        try {
            sourceBefore = sourceStock.decrease(request.quantity());
        } catch (IllegalArgumentException exception) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Transfer quantity cannot exceed available source stock.");
        }

        int targetBefore = targetStock.increase(request.quantity());
        String reason = normalizeOptional(request.reason());

        StockMovement sourceMovement = movementRepository.save(new StockMovement(
                sourceStock,
                StockMovementType.TRANSFER_OUT,
                request.quantity(),
                sourceBefore,
                sourceStock.getQuantity(),
                reason));
        StockMovement targetMovement = movementRepository.save(new StockMovement(
                targetStock,
                StockMovementType.TRANSFER_IN,
                request.quantity(),
                targetBefore,
                targetStock.getQuantity(),
                reason));

        StockTransfer transfer = new StockTransfer(
                sourceStock,
                targetStock,
                request.quantity(),
                reason,
                normalizedIdempotencyKey);
        transfer.attachMovements(sourceMovement, targetMovement);

        return transferMapper.toResponse(transferRepository.save(transfer));
    }

    @Transactional(readOnly = true)
    public StockTransferResponse get(java.util.UUID id) {
        return transferMapper.toResponse(transferRepository.findById(id)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Stock transfer was not found.")));
    }

    @Transactional(readOnly = true)
    public Page<StockTransferResponse> search(StockTransferSearchRequest request, Pageable pageable) {
        Specification<StockTransfer> specification = Specification
                .where(StockTransferSpecifications.productIdEquals(request.productId()))
                .and(StockTransferSpecifications.sourceWarehouseIdEquals(request.sourceWarehouseId()))
                .and(StockTransferSpecifications.targetWarehouseIdEquals(request.targetWarehouseId()))
                .and(StockTransferSpecifications.statusEquals(request.status()))
                .and(StockTransferSpecifications.createdAtGreaterThanOrEqualTo(request.createdFrom()))
                .and(StockTransferSpecifications.createdAtLessThanOrEqualTo(request.createdTo()));

        return transferRepository.findAll(specification, pageable).map(transferMapper::toResponse);
    }

    private void ensureTransferIsAllowed(Stock sourceStock, Warehouse targetWarehouse) {
        stockService.ensureStockOperationIsAllowed(sourceStock);
        if (!targetWarehouse.isActive()) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Transfer cannot target an inactive warehouse.");
        }
        if (sourceStock.getWarehouse().getId().equals(targetWarehouse.getId())) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Source and target warehouses must be different.");
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeIdempotencyKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > 120) {
            throw new DomainException(
                    ErrorCode.VALIDATION_FAILED,
                    HttpStatus.BAD_REQUEST,
                    "Idempotency-Key header cannot exceed 120 characters.");
        }
        return normalized;
    }
}
