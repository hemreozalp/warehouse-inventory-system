package com.hemreozalp.warehouse_inventory_system.application.catalog;

import com.hemreozalp.warehouse_inventory_system.domain.catalog.Category;
import com.hemreozalp.warehouse_inventory_system.web.catalog.CategoryResponse;
import com.hemreozalp.warehouse_inventory_system.web.catalog.CategorySummaryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    CategorySummaryResponse toSummary(Category category);
}
