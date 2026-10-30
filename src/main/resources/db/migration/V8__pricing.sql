-- Fatia 2: configuracao de preco e preco sugerido do orcamento.
-- Valores iniciais sao referencias para o prestador ajustar na tela Empresa > Preço e mão de obra.
create table pricing_settings (
    id                bigint generated always as identity primary key,
    daily_rate        numeric(12, 2) not null,
    indirect_percent  numeric(5, 2)  not null default 0,
    margin_percent    numeric(5, 2)  not null default 0,
    easy_factor       numeric(4, 2)  not null default 0.85,
    normal_factor     numeric(4, 2)  not null default 1.00,
    hard_factor       numeric(4, 2)  not null default 1.25,
    very_hard_factor  numeric(4, 2)  not null default 1.50,
    updated_at        timestamptz    not null default now()
);

insert into pricing_settings (daily_rate, indirect_percent, margin_percent) values (350.00, 10.00, 15.00);

-- Item ligado ao catalogo: quantidade, dificuldade e o calculo gravado no momento em que foi salvo.
alter table estimate_item
    add column difficulty     varchar(20),
    add column labor_days     numeric(10, 3),
    add column labor_cost     numeric(12, 2),
    add column material_cost  numeric(12, 2);

alter table estimate
    add column suggested_total  numeric(12, 2),
    add column suggested_days   numeric(10, 3);
