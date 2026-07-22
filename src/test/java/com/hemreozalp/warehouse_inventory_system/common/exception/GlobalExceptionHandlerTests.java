package com.hemreozalp.warehouse_inventory_system.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hemreozalp.warehouse_inventory_system.common.error.ApiErrorResponse;
import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Stock;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void optimisticLockingFailureReturnsConflictResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/v1/stocks/stock-out");

        ResponseEntity<ApiErrorResponse> response = handler.handleOptimisticLockingFailure(
                new ObjectOptimisticLockingFailureException(Stock.class, UUID.randomUUID()),
                request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.CONCURRENT_MODIFICATION.name(), response.getBody().code());
        assertEquals("/api/v1/stocks/stock-out", response.getBody().path());
    }

    @Test
    void domainExceptionReturnsConfiguredErrorResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/stocks");

        ResponseEntity<ApiErrorResponse> response = handler.handleDomainException(
                new DomainException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        HttpStatus.CONFLICT,
                        "Stock already exists for this product and warehouse."),
                request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.BUSINESS_RULE_VIOLATION.name(), response.getBody().code());
        assertEquals("Stock already exists for this product and warehouse.", response.getBody().message());
        assertEquals("/api/v1/stocks", response.getBody().path());
    }

    @Test
    void unexpectedExceptionReturnsInternalErrorResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/reports/dashboard");

        ResponseEntity<ApiErrorResponse> response = handler.handleException(new RuntimeException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.name(), response.getBody().code());
        assertEquals("Unexpected server error.", response.getBody().message());
        assertEquals("/api/v1/reports/dashboard", response.getBody().path());
    }
}
