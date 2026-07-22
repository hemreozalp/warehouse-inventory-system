create table warehouses (
    id uuid primary key,
    code varchar(50) not null,
    name varchar(150) not null,
    address varchar(500),
    active boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_warehouses_code unique (code)
);

create table stocks (
    id uuid primary key,
    product_id uuid not null,
    warehouse_id uuid not null,
    quantity integer not null,
    version bigint not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_stocks_product_warehouse unique (product_id, warehouse_id),
    constraint fk_stocks_product foreign key (product_id) references products (id),
    constraint fk_stocks_warehouse foreign key (warehouse_id) references warehouses (id),
    constraint ck_stocks_quantity_non_negative check (quantity >= 0)
);

create index idx_warehouses_name on warehouses (name);
create index idx_warehouses_active on warehouses (active);
create index idx_stocks_product_id on stocks (product_id);
create index idx_stocks_warehouse_id on stocks (warehouse_id);
create index idx_stocks_quantity on stocks (quantity);
