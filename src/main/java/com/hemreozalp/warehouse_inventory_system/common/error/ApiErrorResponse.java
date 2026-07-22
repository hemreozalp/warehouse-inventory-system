package com.hemreozalp.warehouse_inventory_system.common.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldViolation> violations
) {
    public static ApiErrorResponse of(int status, String error, String code, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, code, message, path, List.of());
    }

    public static ApiErrorResponse withViolations(
            int status,
            String error,
            String code,
            String message,
            String path,
            List<FieldViolation> violations
    ) {
        return new ApiErrorResponse(Instant.now(), status, error, code, message, path, violations);
    }
}
