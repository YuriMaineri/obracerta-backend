-- Usuarios do sistema. A senha fica so como hash (BCrypt); o primeiro usuario e criado na subida da API.
create table app_user (
    id             bigint generated always as identity primary key,
    name           varchar(150) not null,
    email          varchar(150) not null,
    password_hash  varchar(100) not null,
    active         boolean      not null default true,
    created_at     timestamptz  not null default now(),
    last_login_at  timestamptz
);

create unique index uq_app_user_email on app_user (lower(email));
