package com.hemreozalp.warehouse_inventory_system.application.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hemreozalp.warehouse_inventory_system.domain.catalog.Category;
import com.hemreozalp.warehouse_inventory_system.domain.catalog.Product;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Stock;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Warehouse;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog.CategoryRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.catalog.ProductRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.StockRepository;
import com.hemreozalp.warehouse_inventory_system.infrastructure.persistence.stock.WarehouseRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class StockConcurrencyIntegrationTests {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private StockRepository stockRepository;

    @Test
    void optimisticLockingPreventsLostUpdateOnSameStock() {
        UUID stockId = createStockWithQuantity(5);

        Stock firstTransactionView = loadStock(stockId);
        Stock secondTransactionView = loadStock(stockId);

        decreaseDetachedStock(firstTransactionView, 4);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> decreaseDetachedStock(secondTransactionView, 4));

        assertTrue(hasCause(exception, OptimisticLockingFailureException.class));

        Stock currentStock = loadStock(stockId);
        assertEquals(1, currentStock.getQuantity());
        assertEquals(1, currentStock.getVersion());
    }

    private UUID createStockWithQuantity(int quantity) {
        return transactionTemplate.execute(status -> {
            String suffix = shortId();
            Category category = categoryRepository.save(new Category(
                    "Concurrency Category " + suffix,
                    "Optimistic lock fixture",
                    true));
            Product product = productRepository.save(new Product(
                    "CON-" + suffix,
                    "Concurrency Product " + suffix,
                    "Stock version fixture",
                    category,
                    null,
                    1,
                    true));
            Warehouse warehouse = warehouseRepository.save(new Warehouse(
                    "CON-WH-" + suffix,
                    "Concurrency Warehouse " + suffix,
                    "Lock aisle",
                    true));
            Stock stock = new Stock(product, warehouse);
            stock.increase(quantity);
            return stockRepository.saveAndFlush(stock).getId();
        });
    }

    private Stock loadStock(UUID stockId) {
        return transactionTemplate.execute(status -> stockRepository.findById(stockId).orElseThrow());
    }

    private void decreaseDetachedStock(Stock detachedStock, int quantity) {
        transactionTemplate.executeWithoutResult(status -> {
            Stock stock = stockRepository.save(detachedStock);
            stock.decrease(quantity);
            stockRepository.flush();
        });
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
