create table stock_movements (
    id uuid primary key,
    stock_id uuid not null,
    product_id uuid not null,
    warehouse_id uuid not null,
    type varchar(30) not null,
    quantity integer not null,
    quantity_before integer not null,
    quantity_after integer not null,
    reason varchar(500),
    created_at timestamp with time zone not null,
    constraint fk_stock_movements_stock foreign key (stock_id) references stocks (id),
    constraint fk_stock_movements_product foreign key (product_id) references products (id),
    constraint fk_stock_movements_warehouse foreign key (warehouse_id) references warehouses (id),
    constraint ck_stock_movements_quantity_positive check (quantity > 0),
    constraint ck_stock_movements_quantity_before_non_negative check (quantity_before >= 0),
    constraint ck_stock_movements_quantity_after_non_negative check (quantity_after >= 0)
);

create index idx_stock_movements_stock_id on stock_movements (stock_id);
create index idx_stock_movements_product_id on stock_movements (product_id);
create index idx_stock_movements_warehouse_id on stock_movements (warehouse_id);
create index idx_stock_movements_type on stock_movements (type);
create index idx_stock_movements_created_at on stock_movements (created_at);
