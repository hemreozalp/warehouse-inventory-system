package com.hemreozalp.warehouse_inventory_system.application.stock;

import com.hemreozalp.warehouse_inventory_system.application.catalog.ProductService;
import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.common.exception.DomainException;
import com.hemreozalp.warehouse_inventory_system.domain.catalog.Product;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Stock;
import com.hemreozalp.warehouse_inventory_system.domain.stock.StockMovement;
import com.hemreozalp.warehouse_inventory_system.domain.stock.StockMovementType;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Warehouse;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockMovementRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockSpecifications;
import com.hemreozalp.warehouse_inventory_system.web.stock.CreateStockRequest;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockAdjustmentRequest;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockChangeRequest;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockMovementResponse;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockOperationResponse;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockResponse;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockSearchRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final StockMapper stockMapper;
    private final StockMovementRepository movementRepository;
    private final StockMovementMapper movementMapper;

    public StockService(
            StockRepository stockRepository,
            ProductService productService,
            WarehouseService warehouseService,
            StockMapper stockMapper,
            StockMovementRepository movementRepository,
            StockMovementMapper movementMapper
    ) {
        this.stockRepository = stockRepository;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.stockMapper = stockMapper;
        this.movementRepository = movementRepository;
        this.movementMapper = movementMapper;
    }

    @Transactional
    public StockResponse create(CreateStockRequest request) {
        Product product = productService.findProduct(request.productId());
        Warehouse warehouse = warehouseService.findWarehouse(request.warehouseId());

        ensureProductIsActive(product);
        ensureWarehouseIsActive(warehouse);
        ensureStockDoesNotExist(product.getId(), warehouse.getId());

        Stock stock = new Stock(product, warehouse);
        return stockMapper.toResponse(stockRepository.save(stock));
    }

    @Transactional(readOnly = true)
    public StockResponse get(UUID id) {
        return stockMapper.toResponse(findStock(id));
    }

    @Transactional(readOnly = true)
    public StockResponse getByProductAndWarehouse(UUID productId, UUID warehouseId) {
        return stockMapper.toResponse(stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Stock was not found for product and warehouse.")));
    }

    @Transactional(readOnly = true)
    public Page<StockResponse> search(StockSearchRequest request, Pageable pageable) {
        Specification<Stock> specification = Specification
                .where(StockSpecifications.productIdEquals(request.productId()))
                .and(StockSpecifications.warehouseIdEquals(request.warehouseId()));

        return stockRepository.findAll(specification, pageable).map(stockMapper::toResponse);
    }

    @Transactional
    public StockOperationResponse stockIn(StockChangeRequest request) {
        return stockIn(request, null);
    }

    @Transactional
    public StockOperationResponse stockIn(StockChangeRequest request, String idempotencyKey) {
        String normalizedIdempotencyKey = normalizeIdempotencyKey(idempotencyKey);
        StockOperationResponse existingOperation = findExistingOperation(normalizedIdempotencyKey);
        if (existingOperation != null) {
            return existingOperation;
        }

        Stock stock = findStock(request.stockId());
        ensureStockOperationIsAllowed(stock);

        int before = stock.increase(request.quantity());
        return saveOperation(
                stock,
                StockMovementType.STOCK_IN,
                request.quantity(),
                before,
                normalizeOptional(request.reason()),
                normalizedIdempotencyKey);
    }

    @Transactional
    public StockOperationResponse stockOut(StockChangeRequest request) {
        return stockOut(request, null);
    }

    @Transactional
    public StockOperationResponse stockOut(StockChangeRequest request, String idempotencyKey) {
        String normalizedIdempotencyKey = normalizeIdempotencyKey(idempotencyKey);
        StockOperationResponse existingOperation = findExistingOperation(normalizedIdempotencyKey);
        if (existingOperation != null) {
            return existingOperation;
        }

        Stock stock = findStock(request.stockId());
        ensureStockOperationIsAllowed(stock);

        int before;
        try {
            before = stock.decrease(request.quantity());
        } catch (IllegalArgumentException exception) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Stock out quantity cannot exceed available stock.");
        }

        return saveOperation(
                stock,
                StockMovementType.STOCK_OUT,
                request.quantity(),
                before,
                normalizeOptional(request.reason()),
                normalizedIdempotencyKey);
    }

    @Transactional
    public StockOperationResponse adjust(StockAdjustmentRequest request) {
        return adjust(request, null);
    }

    @Transactional
    public StockOperationResponse adjust(StockAdjustmentRequest request, String idempotencyKey) {
        String normalizedIdempotencyKey = normalizeIdempotencyKey(idempotencyKey);
        StockOperationResponse existingOperation = findExistingOperation(normalizedIdempotencyKey);
        if (existingOperation != null) {
            return existingOperation;
        }

        Stock stock = findStock(request.stockId());
        ensureStockOperationIsAllowed(stock);

        int before = stock.adjustTo(request.quantity());
        int changedQuantity = Math.abs(request.quantity() - before);
        if (changedQuantity == 0) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Adjustment quantity must change current stock.");
        }

        return saveOperation(
                stock,
                StockMovementType.ADJUSTMENT,
                changedQuantity,
                before,
                normalizeOptional(request.reason()),
                normalizedIdempotencyKey);
    }

    public Stock findStock(UUID id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Stock was not found."));
    }

    private void ensureStockDoesNotExist(UUID productId, UUID warehouseId) {
        if (stockRepository.existsByProductIdAndWarehouseId(productId, warehouseId)) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Stock already exists for this product and warehouse.");
        }
    }

    private void ensureProductIsActive(Product product) {
        if (!product.isActive()) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Stock cannot be created for an inactive product.");
        }
    }

    private void ensureWarehouseIsActive(Warehouse warehouse) {
        if (!warehouse.isActive()) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Stock cannot be created for an inactive warehouse.");
        }
    }

    public void ensureStockOperationIsAllowed(Stock stock) {
        if (!stock.getProduct().isActive()) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Stock operation cannot be performed for an inactive product.");
        }
        if (!stock.getWarehouse().isActive()) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Stock operation cannot be performed for an inactive warehouse.");
        }
    }

    private StockOperationResponse saveOperation(
            Stock stock,
            StockMovementType type,
            int quantity,
            int quantityBefore,
            String reason,
            String idempotencyKey
    ) {
        StockMovement movement = new StockMovement(
                stock,
                type,
                quantity,
                quantityBefore,
                stock.getQuantity(),
                reason,
                idempotencyKey);

        StockMovement savedMovement = movementRepository.save(movement);
        return toOperationResponse(savedMovement);
    }

    private StockOperationResponse findExistingOperation(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        return movementRepository.findByIdempotencyKey(idempotencyKey)
                .map(this::toOperationResponse)
                .orElse(null);
    }

    private StockOperationResponse toOperationResponse(StockMovement movement) {
        StockResponse stockResponse = stockMapper.toResponse(movement.getStock());
        StockMovementResponse movementResponse = movementMapper.toResponse(movement);
        return new StockOperationResponse(stockResponse, movementResponse);
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
