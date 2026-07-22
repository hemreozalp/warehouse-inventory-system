create table categories (
    id uuid primary key,
    name varchar(100) not null,
    description varchar(500),
    active boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_categories_name unique (name)
);

create table suppliers (
    id uuid primary key,
    name varchar(150) not null,
    contact_name varchar(120),
    email varchar(255),
    phone varchar(50),
    active boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table products (
    id uuid primary key,
    sku varchar(80) not null,
    name varchar(150) not null,
    description varchar(1000),
    category_id uuid not null,
    supplier_id uuid,
    minimum_stock_level integer not null,
    active boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_products_sku unique (sku),
    constraint fk_products_category foreign key (category_id) references categories (id),
    constraint fk_products_supplier foreign key (supplier_id) references suppliers (id),
    constraint ck_products_minimum_stock_level_non_negative check (minimum_stock_level >= 0)
);

create index idx_categories_active on categories (active);
create index idx_suppliers_name on suppliers (name);
create index idx_suppliers_active on suppliers (active);
create index idx_products_name on products (name);
create index idx_products_category_id on products (category_id);
create index idx_products_supplier_id on products (supplier_id);
create index idx_products_active on products (active);
