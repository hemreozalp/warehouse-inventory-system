package com.hemreozalp.warehouse_inventory_system.web.stock;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StockOperationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void stockInAndOutUpdateQuantityAndCreateMovements() throws Exception {
        String stockId = createStockFixture("OPS-" + shortId());

        stockIn(stockId, 10, "initial receive")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.quantity").value(10))
                .andExpect(jsonPath("$.movement.type").value("STOCK_IN"))
                .andExpect(jsonPath("$.movement.quantity").value(10))
                .andExpect(jsonPath("$.movement.quantityBefore").value(0))
                .andExpect(jsonPath("$.movement.quantityAfter").value(10));

        stockOut(stockId, 4, "shipment")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.quantity").value(6))
                .andExpect(jsonPath("$.movement.type").value("STOCK_OUT"))
                .andExpect(jsonPath("$.movement.quantity").value(4))
                .andExpect(jsonPath("$.movement.quantityBefore").value(10))
                .andExpect(jsonPath("$.movement.quantityAfter").value(6));

        mockMvc.perform(get("/api/v1/stock-movements")
                        .param("stockId", stockId)
                        .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].type").value("STOCK_IN"))
                .andExpect(jsonPath("$.content[1].type").value("STOCK_OUT"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void stockOutCannotExceedAvailableQuantityAndDoesNotCreateMovement() throws Exception {
        String stockId = createStockFixture("NEG-" + shortId());
        stockIn(stockId, 3, "receive").andExpect(status().isOk());

        stockOut(stockId, 4, "too much")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));

        mockMvc.perform(get("/api/v1/stocks/" + stockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));

        mockMvc.perform(get("/api/v1/stock-movements")
                        .param("stockId", stockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].type").value("STOCK_IN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adjustmentSetsQuantityAndRecordsChangedAmount() throws Exception {
        String stockId = createStockFixture("ADJ-" + shortId());
        stockIn(stockId, 8, "receive").andExpect(status().isOk());

        String body = """
                {
                  "stockId": "%s",
                  "quantity": 2,
                  "reason": "cycle count"
                }
                """.formatted(stockId);

        mockMvc.perform(patch("/api/v1/stocks/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.quantity").value(2))
                .andExpect(jsonPath("$.movement.type").value("ADJUSTMENT"))
                .andExpect(jsonPath("$.movement.quantity").value(6))
                .andExpect(jsonPath("$.movement.quantityBefore").value(8))
                .andExpect(jsonPath("$.movement.quantityAfter").value(2));
    }

    @Test
    void movementEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/stock-movements"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private org.springframework.test.web.servlet.ResultActions stockIn(
            String stockId,
            int quantity,
            String reason
    ) throws Exception {
        return stockChange("/api/v1/stocks/stock-in", stockId, quantity, reason);
    }

    private org.springframework.test.web.servlet.ResultActions stockOut(
            String stockId,
            int quantity,
            String reason
    ) throws Exception {
        return stockChange("/api/v1/stocks/stock-out", stockId, quantity, reason);
    }

    private org.springframework.test.web.servlet.ResultActions stockChange(
            String endpoint,
            String stockId,
            int quantity,
            String reason
    ) throws Exception {
        String body = """
                {
                  "stockId": "%s",
                  "quantity": %d,
                  "reason": "%s"
                }
                """.formatted(stockId, quantity, reason);

        return mockMvc.perform(patch(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String createStockFixture(String sku) throws Exception {
        String categoryId = createCategory("Movement Category " + sku);
        String productId = createProduct(sku, categoryId);
        String warehouseId = createWarehouse("WH-" + sku);

        String body = """
                {
                  "productId": "%s",
                  "warehouseId": "%s"
                }
                """.formatted(productId, warehouseId);

        MvcResult result = mockMvc.perform(post("/api/v1/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return extractJsonString(result, "id");
    }

    private String createCategory(String name) throws Exception {
        String body = """
                {
                  "name": "%s",
                  "description": "Movement category",
                  "active": true
                }
                """.formatted(name);

        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return extractJsonString(result, "id");
    }

    private String createProduct(String sku, String categoryId) throws Exception {
        String body = """
                {
                  "sku": "%s",
                  "name": "Movement Product %s",
                  "categoryId": "%s",
                  "minimumStockLevel": 1,
                  "active": true
                }
                """.formatted(sku, sku, categoryId);

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return extractJsonString(result, "id");
    }

    private String createWarehouse(String code) throws Exception {
        String body = """
                {
                  "code": "%s",
                  "name": "Movement Warehouse %s",
                  "address": "Dock",
                  "active": true
                }
                """.formatted(code, code);

        MvcResult result = mockMvc.perform(post("/api/v1/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return extractJsonString(result, "id");
    }

    private String extractJsonString(MvcResult result, String field) throws Exception {
        String body = result.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\\\"" + field + "\\\":\\\"([^\\\"]+)\\\"").matcher(body);
        if (!matcher.find()) {
            throw new IllegalStateException("Field was not found in JSON response: " + field);
        }
        return matcher.group(1);
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
