package com.hemreozalp.warehouse_inventory_system.application.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.StockMovement;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockMovementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    @Mapping(target = "stockId", source = "stock.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    StockMovementResponse toResponse(StockMovement movement);
}
