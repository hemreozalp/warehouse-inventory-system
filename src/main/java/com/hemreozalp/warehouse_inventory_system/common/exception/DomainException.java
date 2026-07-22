package com.hemreozalp.warehouse_inventory_system.common.exception;

import com.hemreozalp.warehouse_inventory_system.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;

    public DomainException(ErrorCode errorCode, HttpStatus status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
