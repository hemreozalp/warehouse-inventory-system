package com.hemreozalp.warehouse_inventory_system.application.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.Warehouse;
import com.hemreozalp.warehouse_inventory_system.web.stock.WarehouseResponse;
import com.hemreozalp.warehouse_inventory_system.web.stock.WarehouseSummaryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    WarehouseResponse toResponse(Warehouse warehouse);

    WarehouseSummaryResponse toSummary(Warehouse warehouse);
}
