package com.hemreozalp.warehouse_inventory_system.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hemreozalp.warehouse_inventory_system.common.error.ApiErrorResponse;
import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import com.hemreozalp.warehouse_inventory_system.domain.stock.Stock;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.mock.web.MockHttpServletRequest;

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
}
