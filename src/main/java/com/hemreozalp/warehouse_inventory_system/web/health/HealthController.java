package com.hemreozalp.warehouse_inventory_system.web.health;

import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    HealthResponse health() {
        return new HealthResponse("UP", Instant.now());
    }
}
