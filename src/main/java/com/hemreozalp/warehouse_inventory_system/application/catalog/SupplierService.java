package com.hemreozalp.warehouse_inventory_system.application.catalog;

import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.common.exception.DomainException;
import com.hemreozalp.warehouse_inventory_system.domain.catalog.Supplier;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog.SupplierRepository;
import com.hemreozalp.warehouse_inventory_system.web.catalog.SupplierRequest;
import com.hemreozalp.warehouse_inventory_system.web.catalog.SupplierResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = new Supplier(
                normalizeRequired(request.name()),
                normalizeOptional(request.contactName()),
                normalizeOptional(request.email()),
                normalizeOptional(request.phone()),
                request.active());

        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(UUID id) {
        return supplierMapper.toResponse(findSupplier(id));
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> list(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(supplierMapper::toResponse);
    }

    @Transactional
    public SupplierResponse update(UUID id, SupplierRequest request) {
        Supplier supplier = findSupplier(id);
        supplier.update(
                normalizeRequired(request.name()),
                normalizeOptional(request.contactName()),
                normalizeOptional(request.email()),
                normalizeOptional(request.phone()),
                request.active());

        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public void delete(UUID id) {
        Supplier supplier = findSupplier(id);
        supplierRepository.delete(supplier);
    }

    public Supplier findSupplier(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Supplier was not found."));
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
