package com.hemreozalp.warehouse_inventory_system.web.catalog;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
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
class ProductCatalogIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProductAndFilterBySku() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String categoryId = createCategory("Hardware " + suffix);
        String supplierId = createSupplier("Main Supplier " + suffix);
        String sku = "SKU-" + suffix;

        String productBody = """
                {
                  "sku": "%s",
                  "name": "Barcode Scanner",
                  "description": "Handheld scanner",
                  "categoryId": "%s",
                  "supplierId": "%s",
                  "minimumStockLevel": 5,
                  "active": true
                }
                """.formatted(sku, categoryId, supplierId);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(sku.toUpperCase()))
                .andExpect(jsonPath("$.category.id").value(categoryId))
                .andExpect(jsonPath("$.supplier.id").value(supplierId))
                .andExpect(jsonPath("$.minimumStockLevel").value(5))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/v1/products")
                        .param("sku", sku)
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "sku,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].sku").value(sku.toUpperCase()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void categoryNameMustBeUnique() throws Exception {
        String name = "Unique Category " + UUID.randomUUID().toString().substring(0, 8);
        createCategory(name);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryBody(name)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void minimumStockLevelCannotBeNegative() throws Exception {
        String categoryId = createCategory("Validation Category " + UUID.randomUUID().toString().substring(0, 8));

        String productBody = """
                {
                  "sku": "NEG-%s",
                  "name": "Invalid Product",
                  "categoryId": "%s",
                  "minimumStockLevel": -1,
                  "active": true
                }
                """.formatted(UUID.randomUUID().toString().substring(0, 8), categoryId);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void catalogEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private String createCategory(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryBody(name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andReturn();

        return extractJsonString(result, "id");
    }

    private String createSupplier(String name) throws Exception {
        String body = """
                {
                  "name": "%s",
                  "contactName": "Ops Lead",
                  "email": "ops-%s@example.com",
                  "phone": "+90 555 010 2030",
                  "active": true
                }
                """.formatted(name, UUID.randomUUID().toString().substring(0, 8));

        MvcResult result = mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andReturn();

        return extractJsonString(result, "id");
    }

    private String categoryBody(String name) {
        return """
                {
                  "name": "%s",
                  "description": "Inventory category",
                  "active": true
                }
                """.formatted(name);
    }

    private String extractJsonString(MvcResult result, String field) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body.replaceAll(".*\\\"" + field + "\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}
