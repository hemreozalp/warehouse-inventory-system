create table users (
    id uuid primary key,
    email varchar(255) not null,
    password_hash varchar(255) not null,
    full_name varchar(120) not null,
    role varchar(30) not null,
    enabled boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_users_email unique (email)
);

create index idx_users_role on users (role);
