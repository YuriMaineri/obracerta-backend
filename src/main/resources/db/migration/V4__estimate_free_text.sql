-- Fatia 1: orcamento em texto livre, no formato dos documentos atuais.
alter table estimate
    add column title       varchar(200),
    add column updated_at  timestamptz not null default now();

alter table estimate_item
    add column internal_note text;

alter table estimate_material
    add column sort_order int not null default 0;

-- Linhas de "VALOR DA MAO DE OBRA E MATERIAL": um orcamento real traz de 1 a 5 valores
-- (mao de obra, material, alternativas), nao um total unico.
create table estimate_price_line (
    id           bigint generated always as identity primary key,
    estimate_id  bigint         not null references estimate (id) on delete cascade,
    description  varchar(250)   not null,
    amount       numeric(12, 2) not null,
    sort_order   int            not null default 0
);

create index idx_estimate_section_estimate on estimate_section (estimate_id);
create index idx_area_section on area (section_id);
create index idx_estimate_material_estimate on estimate_material (estimate_id);
create index idx_estimate_price_line_estimate on estimate_price_line (estimate_id);
create index idx_exclusion_estimate on exclusion (estimate_id);
create index idx_internal_note_estimate on internal_note (estimate_id);
create index idx_estimate_status on estimate (status, issue_date desc);
