-- ObraCerta - esquema inicial
-- Fatia 1: empresa, clientes e orcamento com itens em texto livre.
-- As tabelas de catalogo ja nascem aqui, mas serao preenchidas aos poucos
-- pela propria interface, servico a servico.
-- Nomes em ingles; os comentarios trazem o termo de negocio em portugues.

-- =========================================================
-- MODULO: company (empresa)
-- =========================================================
create table company (
    id          bigint generated always as identity primary key,
    legal_name  varchar(200) not null,          -- razao social
    tax_id      varchar(20),                    -- CNPJ
    phone       varchar(30),
    email       varchar(150),
    address     varchar(250),
    city        varchar(100),
    logo        bytea,
    created_at  timestamptz  not null default now()
);

create table bank_account (
    id              bigint generated always as identity primary key,
    company_id      bigint       not null references company (id),
    bank            varchar(100) not null,
    branch          varchar(20),                -- agencia
    account_number  varchar(30),
    account_type    varchar(30),
    pix_key         varchar(150),
    holder          varchar(200),               -- titular
    is_default      boolean      not null default false,
    active          boolean      not null default true
);

-- Clausulas: garantia, normas regulamentadoras, servicos extras, condicoes de
-- pagamento. Textos hoje redigitados em todo orcamento.
create table clause (
    id          bigint generated always as identity primary key,
    company_id  bigint       not null references company (id),
    type        varchar(30)  not null,
    title       varchar(150) not null,
    content     text         not null,
    is_default  boolean      not null default false,
    sort_order  int          not null default 0
);

-- =========================================================
-- MODULO: customers (clientes)
-- =========================================================
create table customer (
    id              bigint generated always as identity primary key,
    person_type     varchar(20)  not null,      -- INDIVIDUAL (PF) / COMPANY (PJ)
    name            varchar(200) not null,      -- nome ou razao social
    tax_id          varchar(20),                -- CPF ou CNPJ, so digitos
    contact_person  varchar(150),               -- "A/C"
    phone           varchar(30),
    email           varchar(150),
    address         varchar(250),
    district        varchar(100),               -- bairro
    city            varchar(100),
    postal_code     varchar(10),                -- CEP
    active          boolean      not null default true,
    created_at      timestamptz  not null default now()
);

create index idx_customer_name on customer (lower(name));

-- Obra: local onde o servico e executado.
create table job_site (
    id           bigint generated always as identity primary key,
    customer_id  bigint       not null references customer (id),
    description  varchar(250) not null,
    address      varchar(250),
    city         varchar(100)
);

-- =========================================================
-- MODULO: catalog (catalogo, preenchido gradualmente pelo usuario)
-- =========================================================
-- Insumo
create table material (
    id          bigint generated always as identity primary key,
    name        varchar(200)   not null,
    unit        varchar(20)    not null,
    price       numeric(12, 2),
    supplier    varchar(150),
    updated_on  date,
    active      boolean        not null default true
);

-- Historico de preco do insumo
create table material_price_history (
    id              bigint generated always as identity primary key,
    material_id     bigint         not null references material (id),
    price           numeric(12, 2) not null,
    effective_date  date           not null
);

-- Servico
create table service (
    id                    bigint generated always as identity primary key,
    name                  varchar(200) not null,
    category              varchar(80),
    unit                  varchar(20)  not null,
    pricing_mode          varchar(30)  not null,   -- modo de calculo
    proposal_description  text,                    -- texto padrao na proposta
    hours_per_unit        numeric(10, 3),
    hourly_rate           numeric(12, 2),
    usual_price           numeric(12, 2),          -- preco praticado
    active                boolean      not null default true
);

-- Composicao: quanto de cada insumo o servico consome por unidade.
create table service_material (
    id                 bigint generated always as identity primary key,
    service_id         bigint         not null references service (id) on delete cascade,
    material_id        bigint         not null references material (id),
    quantity_per_unit  numeric(12, 4) not null,
    waste_percent      numeric(5, 2)  not null default 0,   -- indice de perda
    unique (service_id, material_id)
);

-- =========================================================
-- MODULO: estimate (orcamento)
-- =========================================================
create table estimate (
    id                       bigint generated always as identity primary key,
    number                   varchar(30)  not null unique,
    customer_id              bigint       not null references customer (id),
    job_site_id              bigint references job_site (id),
    bank_account_id          bigint references bank_account (id),
    issue_date               date         not null default current_date,
    validity_days            int,
    min_lead_days            int,          -- prazo em dias uteis, faixa minima
    max_lead_days            int,          -- prazo em dias uteis, faixa maxima
    material_supply          varchar(30)  not null,   -- regime de material
    estimated_material_cost  numeric(12, 2),
    payment_terms            text,
    status                   varchar(30)  not null default 'DRAFT',
    created_at               timestamptz  not null default now()
);

-- Bloco do orcamento. Um orcamento nao e um total unico: ele tem blocos
-- obrigatorios, opcionais e alternativos entre si (com ou sem pintura das grades).
create table estimate_section (
    id                 bigint generated always as identity primary key,
    estimate_id        bigint       not null references estimate (id) on delete cascade,
    title              varchar(200) not null,
    type               varchar(20)  not null default 'REQUIRED',
    alternative_group  varchar(50),
    sort_order         int          not null default 0
);

-- Ambiente (sala, banheiro, fachada...)
create table area (
    id          bigint generated always as identity primary key,
    section_id  bigint       not null references estimate_section (id) on delete cascade,
    name        varchar(150) not null,
    sort_order  int          not null default 0
);

create table estimate_item (
    id                   bigint generated always as identity primary key,
    area_id              bigint       not null references area (id) on delete cascade,
    service_id           bigint references service (id),
    description          text         not null,
    pricing_mode         varchar(30)  not null,
    measurement          numeric(12, 3),
    quantity             numeric(12, 3),
    calculated_quantity  numeric(12, 3),
    unit_price           numeric(12, 2),
    total_price          numeric(12, 2),
    price_overridden     boolean      not null default false,   -- valor sobrescrito
    sort_order           int          not null default 0
);

create table estimate_material (
    id           bigint generated always as identity primary key,
    estimate_id  bigint         not null references estimate (id) on delete cascade,
    material_id  bigint references material (id),
    description  varchar(200)   not null,
    quantity     numeric(12, 3),
    unit_price   numeric(12, 2),
    total_price  numeric(12, 2),
    source       varchar(20)    not null default 'MANUAL'   -- CALCULATED / MANUAL
);

-- Exclusoes: "Nao incluso os ventiladores", "nao contempla luminarias". Aparece
-- em quase todos os orcamentos reais e hoje e digitado a mao.
create table exclusion (
    id           bigint generated always as identity primary key,
    estimate_id  bigint not null references estimate (id) on delete cascade,
    content      text   not null,
    sort_order   int    not null default 0
);

-- Notas internas: recados tecnicos que hoje vazam para o documento do cliente,
-- em vermelho. Aqui ficam separados e so aparecem na visao interna.
create table internal_note (
    id           bigint generated always as identity primary key,
    estimate_id  bigint not null references estimate (id) on delete cascade,
    item_id      bigint references estimate_item (id) on delete cascade,
    content      text   not null,
    created_at   timestamptz not null default now()
);

create table estimate_clause (
    estimate_id  bigint not null references estimate (id) on delete cascade,
    clause_id    bigint not null references clause (id),
    primary key (estimate_id, clause_id)
);

create index idx_estimate_customer on estimate (customer_id);
create index idx_estimate_item_area on estimate_item (area_id);
