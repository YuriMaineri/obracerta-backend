-- Fatia 2: a mao de obra passa a ser calculada por diaria (decisao da entrevista de 30/09).
-- Produtividade = diarias de um profissional por unidade do servico; o valor da diaria fica nas configuracoes.
alter table service
    add column days_per_unit numeric(10, 4),
    drop column hours_per_unit,
    drop column hourly_rate;

alter table service_material
    add column sort_order int not null default 0;

create index idx_service_material_service on service_material (service_id);
create index idx_material_price_history_material on material_price_history (material_id, effective_date desc);
