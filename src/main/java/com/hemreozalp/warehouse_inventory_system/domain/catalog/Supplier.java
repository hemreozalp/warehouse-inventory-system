package com.hemreozalp.warehouse_inventory_system.domain.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_name")
    private String contactName;

    private String email;

    private String phone;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Supplier() {
    }

    public Supplier(String name, String contactName, String email, String phone, boolean active) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.active = active;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContactName() {
        return contactName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(String name, String contactName, String email, String phone, boolean active) {
        this.name = name;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.active = active;
    }
}
