alter table stocks add column created_by varchar(255);
alter table stocks add column updated_by varchar(255);

alter table stock_movements add column idempotency_key varchar(120);
alter table stock_movements add column created_by varchar(255);

alter table stock_transfers add column idempotency_key varchar(120);
alter table stock_transfers add column created_by varchar(255);

create unique index ux_stock_movements_idempotency_key
    on stock_movements (idempotency_key);

create unique index ux_stock_transfers_idempotency_key
    on stock_transfers (idempotency_key);
