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
class StockTransferIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void transferMovesQuantityAtomicallyAndCreatesTwoMovements() throws Exception {
        String suffix = shortId();
        String categoryId = createCategory("Transfer Category " + suffix);
        String productId = createProduct("TRN-" + suffix, categoryId);
        String sourceWarehouseId = createWarehouse("SRC-" + suffix, true);
        String targetWarehouseId = createWarehouse("TGT-" + suffix, true);
        String sourceStockId = createStock(productId, sourceWarehouseId);
        stockIn(sourceStockId, 12);

        MvcResult transferResult = transfer(sourceStockId, targetWarehouseId, 5, "replenish")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.sourceStock.id").value(sourceStockId))
                .andExpect(jsonPath("$.sourceStock.quantity").value(7))
                .andExpect(jsonPath("$.targetStock.quantity").value(5))
                .andExpect(jsonPath("$.sourceWarehouse.id").value(sourceWarehouseId))
                .andExpect(jsonPath("$.targetWarehouse.id").value(targetWarehouseId))
                .andExpect(jsonPath("$.sourceMovement.type").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$.targetMovement.type").value("TRANSFER_IN"))
                .andReturn();

        String transferId = extractJsonString(transferResult, "id");
        String targetStockId = extractJsonString(transferResult, "targetStock", "id");

        mockMvc.perform(get("/api/v1/stock-transfers/" + transferId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transferId))
                .andExpect(jsonPath("$.targetStock.id").value(targetStockId));

        mockMvc.perform(get("/api/v1/stocks/" + sourceStockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(7));

        mockMvc.perform(get("/api/v1/stocks/" + targetStockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));

        mockMvc.perform(get("/api/v1/stock-movements")
                        .param("stockId", sourceStockId)
                        .param("type", "TRANSFER_OUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/v1/stock-movements")
                        .param("stockId", targetStockId)
                        .param("type", "TRANSFER_IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void transferCannotTargetSameWarehouse() throws Exception {
        String suffix = shortId();
        String categoryId = createCategory("Same Warehouse Category " + suffix);
        String productId = createProduct("SAM-" + suffix, categoryId);
        String warehouseId = createWarehouse("SAM-WH-" + suffix, true);
        String sourceStockId = createStock(productId, warehouseId);
        stockIn(sourceStockId, 6);

        transfer(sourceStockId, warehouseId, 2, "same place")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Source and target warehouses must be different."));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void transferWithSameIdempotencyKeyIsAppliedOnlyOnce() throws Exception {
        String suffix = shortId();
        String categoryId = createCategory("Idempotent Transfer Category " + suffix);
        String productId = createProduct("IDT-" + suffix, categoryId);
        String sourceWarehouseId = createWarehouse("IDT-SRC-" + suffix, true);
        String targetWarehouseId = createWarehouse("IDT-TGT-" + suffix, true);
        String sourceStockId = createStock(productId, sourceWarehouseId);
        String idempotencyKey = "transfer-" + shortId();
        stockIn(sourceStockId, 10);

        MvcResult firstResult = transfer(sourceStockId, targetWarehouseId, 4, "move once", idempotencyKey)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceStock.quantity").value(6))
                .andExpect(jsonPath("$.targetStock.quantity").value(4))
                .andExpect(jsonPath("$.idempotencyKey").value(idempotencyKey))
                .andExpect(jsonPath("$.createdBy").value("admin@example.com"))
                .andReturn();
        String transferId = extractJsonString(firstResult, "id");
        String targetStockId = extractJsonString(firstResult, "targetStock", "id");

        transfer(sourceStockId, targetWarehouseId, 4, "move once", idempotencyKey)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transferId))
                .andExpect(jsonPath("$.sourceStock.quantity").value(6))
                .andExpect(jsonPath("$.targetStock.quantity").value(4));

        mockMvc.perform(get("/api/v1/stocks/" + sourceStockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(6));
        mockMvc.perform(get("/api/v1/stocks/" + targetStockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4));
        mockMvc.perform(get("/api/v1/stock-transfers")
                        .param("sourceWarehouseId", sourceWarehouseId)
                        .param("targetWarehouseId", targetWarehouseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void insufficientTransferDoesNotCreateTargetStockOrMovements() throws Exception {
        String suffix = shortId();
        String categoryId = createCategory("Insufficient Transfer Category " + suffix);
        String productId = createProduct("ITR-" + suffix, categoryId);
        String sourceWarehouseId = createWarehouse("ITR-SRC-" + suffix, true);
        String targetWarehouseId = createWarehouse("ITR-TGT-" + suffix, true);
        String sourceStockId = createStock(productId, sourceWarehouseId);
        stockIn(sourceStockId, 3);

        transfer(sourceStockId, targetWarehouseId, 4, "too much")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Transfer quantity cannot exceed available source stock."));

        mockMvc.perform(get("/api/v1/stocks/" + sourceStockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));

        mockMvc.perform(get("/api/v1/stocks")
                        .param("productId", productId)
                        .param("warehouseId", targetWarehouseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        mockMvc.perform(get("/api/v1/stock-movements")
                        .param("stockId", sourceStockId)
                        .param("type", "TRANSFER_OUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void transferEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/stock-transfers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private org.springframework.test.web.servlet.ResultActions transfer(
            String sourceStockId,
            String targetWarehouseId,
            int quantity,
            String reason
    ) throws Exception {
        String body = """
                {
                  "sourceStockId": "%s",
                  "targetWarehouseId": "%s",
                  "quantity": %d,
                  "reason": "%s"
                }
                """.formatted(sourceStockId, targetWarehouseId, quantity, reason);

        return mockMvc.perform(post("/api/v1/stock-transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private org.springframework.test.web.servlet.ResultActions transfer(
            String sourceStockId,
            String targetWarehouseId,
            int quantity,
            String reason,
            String idempotencyKey
    ) throws Exception {
        String body = """
                {
                  "sourceStockId": "%s",
                  "targetWarehouseId": "%s",
                  "quantity": %d,
                  "reason": "%s"
                }
                """.formatted(sourceStockId, targetWarehouseId, quantity, reason);

        return mockMvc.perform(post("/api/v1/stock-transfers")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void stockIn(String stockId, int quantity) throws Exception {
        String body = """
                {
                  "stockId": "%s",
                  "quantity": %d,
                  "reason": "seed"
                }
                """.formatted(stockId, quantity);

        mockMvc.perform(patch("/api/v1/stocks/stock-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private String createStock(String productId, String warehouseId) throws Exception {
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
                  "description": "Transfer category",
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
                  "name": "Transfer Product %s",
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

    private String createWarehouse(String code, boolean active) throws Exception {
        String body = """
                {
                  "code": "%s",
                  "name": "Transfer Warehouse %s",
                  "address": "Transfer Dock",
                  "active": %s
                }
                """.formatted(code, code, active);

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

    private String extractJsonString(MvcResult result, String objectField, String field) throws Exception {
        String body = result.getResponse().getContentAsString();
        Matcher matcher = Pattern
                .compile("\\\"" + objectField + "\\\":\\{.*?\\\"" + field + "\\\":\\\"([^\\\"]+)\\\"")
                .matcher(body);
        if (!matcher.find()) {
            throw new IllegalStateException("Nested field was not found in JSON response: " + objectField + "." + field);
        }
        return matcher.group(1);
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
