package com.hemreozalp.warehouse_inventory_system.application.catalog;

import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.common.exception.DomainException;
import com.hemreozalp.warehouse_inventory_system.domain.catalog.Category;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog.CategoryRepository;
import com.hemreozalp.warehouse_inventory_system.web.catalog.CategoryRequest;
import com.hemreozalp.warehouse_inventory_system.web.catalog.CategoryResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = normalizeRequired(request.name());
        ensureNameIsUnique(name);

        Category category = new Category(name, normalizeOptional(request.description()), request.active());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(UUID id) {
        return categoryMapper.toResponse(findCategory(id));
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toResponse);
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = findCategory(id);
        String name = normalizeRequired(request.name());
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw duplicateCategory();
        }

        category.update(name, normalizeOptional(request.description()), request.active());
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(UUID id) {
        Category category = findCategory(id);
        categoryRepository.delete(category);
    }

    public Category findCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Category was not found."));
    }

    private void ensureNameIsUnique(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw duplicateCategory();
        }
    }

    private DomainException duplicateCategory() {
        return new DomainException(
                ErrorCode.BUSINESS_RULE_VIOLATION,
                HttpStatus.CONFLICT,
                "Category name is already in use.");
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
