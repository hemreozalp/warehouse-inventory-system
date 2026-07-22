package com.hemreozalp.warehouse_inventory_system.application.stock;

import com.hemreozalp.warehouse_inventory_system.application.catalog.ProductMapper;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Stock;
import com.hemreozalp.warehouse_inventory_system.web.stock.StockResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, WarehouseMapper.class})
public interface StockMapper {

    StockResponse toResponse(Stock stock);
}
