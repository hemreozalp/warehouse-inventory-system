package com.hemreozalp.warehouse_inventory_system.application.catalog;

import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.common.exception.DomainException;
import com.hemreozalp.warehouse_inventory_system.domain.catalog.Category;
import com.hemreozalp.warehouse_inventory_system.domain.catalog.Product;
import com.hemreozalp.warehouse_inventory_system.domain.catalog.Supplier;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog.ProductRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog.ProductSpecifications;
import com.hemreozalp.warehouse_inventory_system.web.catalog.ProductRequest;
import com.hemreozalp.warehouse_inventory_system.web.catalog.ProductResponse;
import com.hemreozalp.warehouse_inventory_system.web.catalog.ProductSearchRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            CategoryService categoryService,
            SupplierService supplierService,
            ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String sku = normalizeRequired(request.sku()).toUpperCase();
        ensureSkuIsUnique(sku);

        Category category = categoryService.findCategory(request.categoryId());
        Supplier supplier = findSupplierIfPresent(request.supplierId());
        ensureCategoryIsActive(category);
        ensureSupplierIsActive(supplier);

        Product product = new Product(
                sku,
                normalizeRequired(request.name()),
                normalizeOptional(request.description()),
                category,
                supplier,
                request.minimumStockLevel(),
                request.active());

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID id) {
        return productMapper.toResponse(findProduct(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(ProductSearchRequest request, Pageable pageable) {
        Specification<Product> specification = Specification
                .where(ProductSpecifications.skuContains(request.sku()))
                .and(ProductSpecifications.nameContains(request.name()))
                .and(ProductSpecifications.categoryIdEquals(request.categoryId()))
                .and(ProductSpecifications.supplierIdEquals(request.supplierId()))
                .and(ProductSpecifications.activeEquals(request.active()));

        return productRepository.findAll(specification, pageable).map(productMapper::toResponse);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = findProduct(id);
        String sku = normalizeRequired(request.sku()).toUpperCase();
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
            throw duplicateSku();
        }

        Category category = categoryService.findCategory(request.categoryId());
        Supplier supplier = findSupplierIfPresent(request.supplierId());
        ensureCategoryIsActive(category);
        ensureSupplierIsActive(supplier);

        product.update(
                sku,
                normalizeRequired(request.name()),
                normalizeOptional(request.description()),
                category,
                supplier,
                request.minimumStockLevel(),
                request.active());

        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse activate(UUID id) {
        Product product = findProduct(id);
        product.activate();
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse deactivate(UUID id) {
        Product product = findProduct(id);
        product.deactivate();
        return productMapper.toResponse(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = findProduct(id);
        productRepository.delete(product);
    }

    public Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Product was not found."));
    }

    private Supplier findSupplierIfPresent(UUID supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierService.findSupplier(supplierId);
    }

    private void ensureSkuIsUnique(String sku) {
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw duplicateSku();
        }
    }

    private void ensureCategoryIsActive(Category category) {
        if (!category.isActive()) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Products cannot be assigned to an inactive category.");
        }
    }

    private void ensureSupplierIsActive(Supplier supplier) {
        if (supplier != null && !supplier.isActive()) {
            throw new DomainException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    HttpStatus.CONFLICT,
                    "Products cannot be assigned to an inactive supplier.");
        }
    }

    private DomainException duplicateSku() {
        return new DomainException(
                ErrorCode.BUSINESS_RULE_VIOLATION,
                HttpStatus.CONFLICT,
                "Product SKU is already in use.");
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
