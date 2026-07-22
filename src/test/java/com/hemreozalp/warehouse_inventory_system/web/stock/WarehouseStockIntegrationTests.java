package com.hemreozalp.warehouse_inventory_system.web.stock;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class WarehouseStockIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createWarehouseAndStockForProductWarehousePair() throws Exception {
        String suffix = shortId();
        String categoryId = createCategory("Stock Category " + suffix);
        String productId = createProduct("STK-" + suffix, categoryId, true);
        String warehouseId = createWarehouse("WH-" + suffix, true);

        MvcResult stockResult = createStock(productId, warehouseId)
                .andExpect(jsonPath("$.product.id").value(productId))
                .andExpect(jsonPath("$.warehouse.id").value(warehouseId))
                .andExpect(jsonPath("$.quantity").value(0))
                .andExpect(jsonPath("$.version").value(0))
                .andReturn();
        String stockId = extractJsonString(stockResult, "id");

        mockMvc.perform(get("/api/v1/stocks/" + stockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stockId))
                .andExpect(jsonPath("$.quantity").value(0));

        mockMvc.perform(get("/api/v1/stocks")
                        .param("productId", productId)
                        .param("warehouseId", warehouseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(stockId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void productWarehouseStockMustBeUnique() throws Exception {
        String suffix = shortId();
        String categoryId = createCategory("Unique Stock Category " + suffix);
        String productId = createProduct("UNQ-" + suffix, categoryId, true);
        String warehouseId = createWarehouse("UNQ-WH-" + suffix, true);

        createStock(productId, warehouseId);

        createStock(productId, warehouseId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void stockCannotBeCreatedForInactiveProductOrWarehouse() throws Exception {
        String suffix = shortId();
        String categoryId = createCategory("Inactive Stock Category " + suffix);
        String activeWarehouseId = createWarehouse("IA-WH-" + suffix, true);
        String inactiveWarehouseId = createWarehouse("IN-WH-" + suffix, false);
        String inactiveProductId = createProduct("INP-" + suffix, categoryId, false);
        String activeProductId = createProduct("ACP-" + suffix, categoryId, true);

        createStock(inactiveProductId, activeWarehouseId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Stock cannot be created for an inactive product."));

        createStock(activeProductId, inactiveWarehouseId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Stock cannot be created for an inactive warehouse."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void warehouseCodeMustBeUnique() throws Exception {
        String code = "DUP-" + shortId();
        createWarehouse(code, true);

        mockMvc.perform(post("/api/v1/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(warehouseBody(code, true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void stockEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/stocks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private ResultActionsWrapper createStock(String productId, String warehouseId) throws Exception {
        String body = """
                {
                  "productId": "%s",
                  "warehouseId": "%s"
                }
                """.formatted(productId, warehouseId);

        return new ResultActionsWrapper(mockMvc.perform(post("/api/v1/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)));
    }

    private String createWarehouse(String code, boolean active) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(warehouseBody(code, active)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.code").value(code.toUpperCase()))
                .andReturn();

        return extractJsonString(result, "id");
    }

    private String createCategory(String name) throws Exception {
        String body = """
                {
                  "name": "%s",
                  "description": "Stock model category",
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

    private String createProduct(String sku, String categoryId, boolean active) throws Exception {
        String body = """
                {
                  "sku": "%s",
                  "name": "Stocked Product %s",
                  "categoryId": "%s",
                  "minimumStockLevel": 3,
                  "active": %s
                }
                """.formatted(sku, sku, categoryId, active);

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return extractJsonString(result, "id");
    }

    private String warehouseBody(String code, boolean active) {
        return """
                {
                  "code": "%s",
                  "name": "Warehouse %s",
                  "address": "Industrial Zone",
                  "active": %s
                }
                """.formatted(code, code, active);
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

    private record ResultActionsWrapper(org.springframework.test.web.servlet.ResultActions resultActions) {

        ResultActionsWrapper andExpect(org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
            resultActions.andExpect(matcher);
            return this;
        }

        MvcResult andReturn() {
            return resultActions.andReturn();
        }
    }
}
