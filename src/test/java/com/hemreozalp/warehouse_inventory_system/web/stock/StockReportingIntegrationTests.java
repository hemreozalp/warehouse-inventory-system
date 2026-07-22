package com.hemreozalp.warehouse_inventory_system.web.stock;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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
class StockReportingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void lowStockReportShowsItemsAtOrBelowProductMinimumStockLevel() throws Exception {
        StockFixture fixture = createStockFixture("LOW-" + shortId(), 5);

        mockMvc.perform(get("/api/v1/reports/low-stock")
                        .param("productId", fixture.productId())
                        .param("warehouseId", fixture.warehouseId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].stockId").value(fixture.stockId()))
                .andExpect(jsonPath("$.content[0].productId").value(fixture.productId()))
                .andExpect(jsonPath("$.content[0].warehouseId").value(fixture.warehouseId()))
                .andExpect(jsonPath("$.content[0].quantity").value(0))
                .andExpect(jsonPath("$.content[0].minimumStockLevel").value(5))
                .andExpect(jsonPath("$.content[0].shortageQuantity").value(5));

        stockIn(fixture.stockId(), 6, "replenish");

        mockMvc.perform(get("/api/v1/reports/low-stock")
                        .param("productId", fixture.productId())
                        .param("warehouseId", fixture.warehouseId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboardMetricsReturnInventorySummary() throws Exception {
        StockFixture fixture = createStockFixture("DASH-" + shortId(), 5);
        stockIn(fixture.stockId(), 2, "dashboard receive");

        mockMvc.perform(get("/api/v1/reports/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProductCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.activeWarehouseCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.stockItemCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalQuantityOnHand").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.lowStockItemCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.movementCount").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void movementHistoryCanBeFilteredByProductWarehouseAndDateRange() throws Exception {
        StockFixture fixture = createStockFixture("HIST-" + shortId(), 1);
        String createdFrom = Instant.now().minusSeconds(60).toString();
        stockIn(fixture.stockId(), 3, "history receive");
        String createdTo = Instant.now().plusSeconds(60).toString();

        mockMvc.perform(get("/api/v1/stock-movements")
                        .param("productId", fixture.productId())
                        .param("warehouseId", fixture.warehouseId())
                        .param("createdFrom", createdFrom)
                        .param("createdTo", createdTo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].stockId").value(fixture.stockId()))
                .andExpect(jsonPath("$.content[0].productId").value(fixture.productId()))
                .andExpect(jsonPath("$.content[0].warehouseId").value(fixture.warehouseId()))
                .andExpect(jsonPath("$.content[0].type").value("STOCK_IN"));
    }

    @Test
    void reportEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/reports/low-stock"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/reports/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private void stockIn(String stockId, int quantity, String reason) throws Exception {
        String body = """
                {
                  "stockId": "%s",
                  "quantity": %d,
                  "reason": "%s"
                }
                """.formatted(stockId, quantity, reason);

        mockMvc.perform(patch("/api/v1/stocks/stock-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private StockFixture createStockFixture(String sku, int minimumStockLevel) throws Exception {
        String categoryId = createCategory("Report Category " + sku);
        String productId = createProduct(sku, categoryId, minimumStockLevel);
        String warehouseId = createWarehouse("RPT-WH-" + sku);

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

        return new StockFixture(productId, warehouseId, extractJsonString(result, "id"));
    }

    private String createCategory(String name) throws Exception {
        String body = """
                {
                  "name": "%s",
                  "description": "Reporting category",
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

    private String createProduct(String sku, String categoryId, int minimumStockLevel) throws Exception {
        String body = """
                {
                  "sku": "%s",
                  "name": "Reporting Product %s",
                  "categoryId": "%s",
                  "minimumStockLevel": %d,
                  "active": true
                }
                """.formatted(sku, sku, categoryId, minimumStockLevel);

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
                  "name": "Reporting Warehouse %s",
                  "address": "Report dock",
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

    private record StockFixture(String productId, String warehouseId, String stockId) {
    }
}
