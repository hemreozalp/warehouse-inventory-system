package com.hemreozalp.warehouse_inventory_system.application.stock;

import com.hemreozalp.warehouse_inventory_system.application.catalog.ProductService;
import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.common.exception.DomainException;
import com.hemreozalp.warehouse_inventory_system.domain.catalog.Product;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Stock;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Warehouse;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockSpecifications;
import com.hemreozalp.warehouse_inventory_system.web.stock.CreateStockRequest;
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

    public StockService(
            StockRepository stockRepository,
            ProductService productService,
            WarehouseService warehouseService,
            StockMapper stockMapper
    ) {
        this.stockRepository = stockRepository;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.stockMapper = stockMapper;
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
}
