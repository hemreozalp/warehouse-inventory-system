package com.hemreozalp.warehouse_inventory_system.application.stock;

import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.common.exception.DomainException;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Warehouse;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.WarehouseRepository;
import com.hemreozalp.warehouse_inventory_system.web.stock.WarehouseRequest;
import com.hemreozalp.warehouse_inventory_system.web.stock.WarehouseResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    public WarehouseService(WarehouseRepository warehouseRepository, WarehouseMapper warehouseMapper) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseMapper = warehouseMapper;
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        String code = normalizeCode(request.code());
        ensureCodeIsUnique(code);

        Warehouse warehouse = new Warehouse(
                code,
                normalizeRequired(request.name()),
                normalizeOptional(request.address()),
                request.active());

        return warehouseMapper.toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public WarehouseResponse get(UUID id) {
        return warehouseMapper.toResponse(findWarehouse(id));
    }

    @Transactional(readOnly = true)
    public Page<WarehouseResponse> list(Pageable pageable) {
        return warehouseRepository.findAll(pageable).map(warehouseMapper::toResponse);
    }

    @Transactional
    public WarehouseResponse update(UUID id, WarehouseRequest request) {
        Warehouse warehouse = findWarehouse(id);
        String code = normalizeCode(request.code());
        if (warehouseRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw duplicateWarehouseCode();
        }

        warehouse.update(
                code,
                normalizeRequired(request.name()),
                normalizeOptional(request.address()),
                request.active());

        return warehouseMapper.toResponse(warehouse);
    }

    @Transactional
    public void delete(UUID id) {
        Warehouse warehouse = findWarehouse(id);
        warehouseRepository.delete(warehouse);
    }

    public Warehouse findWarehouse(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Warehouse was not found."));
    }

    private void ensureCodeIsUnique(String code) {
        if (warehouseRepository.existsByCodeIgnoreCase(code)) {
            throw duplicateWarehouseCode();
        }
    }

    private DomainException duplicateWarehouseCode() {
        return new DomainException(
                ErrorCode.BUSINESS_RULE_VIOLATION,
                HttpStatus.CONFLICT,
                "Warehouse code is already in use.");
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
