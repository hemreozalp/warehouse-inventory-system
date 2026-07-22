package com.hemreozalp.warehouse_inventory_system.domain.stock;

import com.hemreozalp.warehouse_inventory_system.domain.catalog.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "stock_transfers")
public class StockTransfer {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_stock_id", nullable = false)
    private Stock sourceStock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_stock_id", nullable = false)
    private Stock targetStock;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_warehouse_id", nullable = false)
    private Warehouse sourceWarehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_warehouse_id", nullable = false)
    private Warehouse targetWarehouse;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockTransferStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_movement_id")
    private StockMovement sourceMovement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_movement_id")
    private StockMovement targetMovement;

    @Column(length = 500)
    private String reason;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    protected StockTransfer() {
    }

    public StockTransfer(Stock sourceStock, Stock targetStock, int quantity, String reason) {
        this(sourceStock, targetStock, quantity, reason, null);
    }

    public StockTransfer(Stock sourceStock, Stock targetStock, int quantity, String reason, String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.product = sourceStock.getProduct();
        this.sourceStock = sourceStock;
        this.targetStock = targetStock;
        this.sourceWarehouse = sourceStock.getWarehouse();
        this.targetWarehouse = targetStock.getWarehouse();
        this.quantity = quantity;
        this.status = StockTransferStatus.COMPLETED;
        this.reason = reason;
        this.idempotencyKey = idempotencyKey;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public void attachMovements(StockMovement sourceMovement, StockMovement targetMovement) {
        this.sourceMovement = sourceMovement;
        this.targetMovement = targetMovement;
    }

    public UUID getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Stock getSourceStock() {
        return sourceStock;
    }

    public Stock getTargetStock() {
        return targetStock;
    }

    public Warehouse getSourceWarehouse() {
        return sourceWarehouse;
    }

    public Warehouse getTargetWarehouse() {
        return targetWarehouse;
    }

    public int getQuantity() {
        return quantity;
    }

    public StockTransferStatus getStatus() {
        return status;
    }

    public StockMovement getSourceMovement() {
        return sourceMovement;
    }

    public StockMovement getTargetMovement() {
        return targetMovement;
    }

    public String getReason() {
        return reason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
