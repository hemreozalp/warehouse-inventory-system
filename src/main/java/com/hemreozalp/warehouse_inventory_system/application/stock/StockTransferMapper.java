package com.hemreozalp.warehouse_inventory_system.application.stock;

import com.hemreozalp.warehouse_inventory_system.domain.stock.StockTransfer;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockTransferResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {StockMapper.class, WarehouseMapper.class, StockMovementMapper.class})
public interface StockTransferMapper {

    @Mapping(target = "productId", source = "product.id")
    StockTransferResponse toResponse(StockTransfer transfer);
}
