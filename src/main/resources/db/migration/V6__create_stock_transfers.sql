create table stock_transfers (
    id uuid primary key,
    product_id uuid not null,
    source_stock_id uuid not null,
    target_stock_id uuid not null,
    source_warehouse_id uuid not null,
    target_warehouse_id uuid not null,
    quantity integer not null,
    status varchar(30) not null,
    source_movement_id uuid,
    target_movement_id uuid,
    reason varchar(500),
    created_at timestamp with time zone not null,
    constraint fk_stock_transfers_product foreign key (product_id) references products (id),
    constraint fk_stock_transfers_source_stock foreign key (source_stock_id) references stocks (id),
    constraint fk_stock_transfers_target_stock foreign key (target_stock_id) references stocks (id),
    constraint fk_stock_transfers_source_warehouse foreign key (source_warehouse_id) references warehouses (id),
    constraint fk_stock_transfers_target_warehouse foreign key (target_warehouse_id) references warehouses (id),
    constraint fk_stock_transfers_source_movement foreign key (source_movement_id) references stock_movements (id),
    constraint fk_stock_transfers_target_movement foreign key (target_movement_id) references stock_movements (id),
    constraint ck_stock_transfers_quantity_positive check (quantity > 0),
    constraint ck_stock_transfers_distinct_warehouses check (source_warehouse_id <> target_warehouse_id)
);

create index idx_stock_transfers_product_id on stock_transfers (product_id);
create index idx_stock_transfers_source_stock_id on stock_transfers (source_stock_id);
create index idx_stock_transfers_target_stock_id on stock_transfers (target_stock_id);
create index idx_stock_transfers_source_warehouse_id on stock_transfers (source_warehouse_id);
create index idx_stock_transfers_target_warehouse_id on stock_transfers (target_warehouse_id);
create index idx_stock_transfers_status on stock_transfers (status);
create index idx_stock_transfers_created_at on stock_transfers (created_at);
