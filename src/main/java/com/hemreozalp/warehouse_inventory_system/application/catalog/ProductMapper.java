package com.hemreozalp.warehouse_inventory_system.application.catalog;

import com.hemreozalp.warehouse_inventory_system.domain.catalog.Product;
import com.hemreozalp.warehouse_inventory_system.web.catalog.ProductResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, SupplierMapper.class})
public interface ProductMapper {

    ProductResponse toResponse(Product product);
}
