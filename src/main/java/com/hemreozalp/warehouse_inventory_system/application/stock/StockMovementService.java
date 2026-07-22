package com.hemreozalp.warehouse_inventory_system.application.stock;

import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.common.exception.DomainException;
import com.hemreozalp.warehouse_inventory_system.domain.stock.StockMovement;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockMovementRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockMovementSpecifications;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockMovementResponse;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockMovementSearchRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final StockMovementMapper movementMapper;

    public StockMovementService(StockMovementRepository movementRepository, StockMovementMapper movementMapper) {
        this.movementRepository = movementRepository;
        this.movementMapper = movementMapper;
    }

    @Transactional(readOnly = true)
    public StockMovementResponse get(UUID id) {
        return movementMapper.toResponse(movementRepository.findById(id)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Stock movement was not found.")));
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> search(StockMovementSearchRequest request, Pageable pageable) {
        Specification<StockMovement> specification = Specification
                .where(StockMovementSpecifications.stockIdEquals(request.stockId()))
                .and(StockMovementSpecifications.productIdEquals(request.productId()))
                .and(StockMovementSpecifications.warehouseIdEquals(request.warehouseId()))
                .and(StockMovementSpecifications.typeEquals(request.type()))
                .and(StockMovementSpecifications.createdAtGreaterThanOrEqualTo(request.createdFrom()))
                .and(StockMovementSpecifications.createdAtLessThanOrEqualTo(request.createdTo()));

        return movementRepository.findAll(specification, pageable).map(movementMapper::toResponse);
    }
}
