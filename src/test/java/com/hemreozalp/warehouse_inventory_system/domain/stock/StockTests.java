package com.hemreozalp.warehouse_inventory_system.domain.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hemreozalp.warehouse_inventory_system.domain.catalog.Category;
import com.hemreozalp.warehouse_inventory_system.domain.catalog.Product;
import org.junit.jupiter.api.Test;

class StockTests {

    @Test
    void increaseAddsQuantityAndReturnsPreviousQuantity() {
        Stock stock = stockFixture();

        int before = stock.increase(7);

        assertEquals(0, before);
        assertEquals(7, stock.getQuantity());
    }

    @Test
    void decreaseSubtractsQuantityAndReturnsPreviousQuantity() {
        Stock stock = stockFixture();
        stock.increase(10);

        int before = stock.decrease(4);

        assertEquals(10, before);
        assertEquals(6, stock.getQuantity());
    }

    @Test
    void decreaseCannotMakeQuantityNegative() {
        Stock stock = stockFixture();
        stock.increase(3);

        assertThrows(IllegalArgumentException.class, () -> stock.decrease(4));
        assertEquals(3, stock.getQuantity());
    }

    @Test
    void adjustmentCannotUseNegativeQuantity() {
        Stock stock = stockFixture();

        assertThrows(IllegalArgumentException.class, () -> stock.adjustTo(-1));
        assertEquals(0, stock.getQuantity());
    }

    @Test
    void zeroOrNegativeChangesAreRejected() {
        Stock stock = stockFixture();

        assertThrows(IllegalArgumentException.class, () -> stock.increase(0));
        assertThrows(IllegalArgumentException.class, () -> stock.decrease(0));
        assertThrows(IllegalArgumentException.class, () -> stock.increase(-1));
        assertThrows(IllegalArgumentException.class, () -> stock.decrease(-1));
    }

    private Stock stockFixture() {
        Category category = new Category("Unit Category", "Unit test category", true);
        Product product = new Product(
                "UNIT-SKU",
                "Unit Product",
                "Unit test product",
                category,
                null,
                1,
                true);
        Warehouse warehouse = new Warehouse("UNIT-WH", "Unit Warehouse", "Unit aisle", true);
        return new Stock(product, warehouse);
    }
}
