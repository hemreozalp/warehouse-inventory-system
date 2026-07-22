package com.hemreozalp.warehouse_inventory_system.application.catalog;

import com.hemreozalp.warehouse_inventory_system.domain.catalog.Supplier;
import com.hemreozalp.warehouse_inventory_system.web.catalog.SupplierResponse;
import com.hemreozalp.warehouse_inventory_system.web.catalog.SupplierSummaryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierResponse toResponse(Supplier supplier);

    SupplierSummaryResponse toSummary(Supplier supplier);
}
