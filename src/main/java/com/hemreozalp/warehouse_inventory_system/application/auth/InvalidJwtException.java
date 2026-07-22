package com.hemreozalp.warehouse_inventory_system.application.auth;

public class InvalidJwtException extends RuntimeException {

    public InvalidJwtException(String message) {
        super(message);
    }
}
