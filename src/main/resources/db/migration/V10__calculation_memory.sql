-- Fatia 2: memoria de calculo (US040) e arredondamento para embalagem fechada (US031).

-- Embalagem em que o insumo e vendido ("galao de 3,6 L"). Opcional; os dois campos andam juntos.
alter table material
    add column package_name  varchar(30),
    add column package_size  numeric(10, 3),
    add constraint ck_material_package check ((package_name is null) = (package_size is null) and (package_size is null or package_size > 0));

-- Referencias iniciais, para o prestador revisar na tela.
update material m
set package_name = p.package_name, package_size = p.package_size
from (values
    ('Tinta acrílica fosca', 'galão', 3.6),
    ('Tinta látex PVA', 'galão', 3.6),
    ('Selador acrílico', 'galão', 3.6),
    ('Esmalte sintético', 'galão', 3.6),
    ('Fundo anticorrosivo (zarcão)', 'galão', 3.6),
    ('Aguarrás', 'lata', 5),
    ('Massa corrida PVA', 'balde', 25),
    ('Textura acrílica', 'balde', 25),
    ('Massa para drywall', 'balde', 15),
    ('Manta líquida acrílica', 'balde', 18),
    ('Rejunte', 'pacote', 1),
    ('Cabo flexível 2,5 mm²', 'rolo', 100),
    ('Eletroduto corrugado 3/4"', 'rolo', 50)
) as p (name, package_name, package_size)
where m.name = p.name;

-- Insumos de cada servico do orcamento, como estavam no catalogo no momento em que foi salvo.
create table estimate_item_material (
    id                 bigint generated always as identity primary key,
    item_id            bigint         not null references estimate_item (id) on delete cascade,
    material_id        bigint references material (id),
    material_name      varchar(200)   not null,
    unit               varchar(20)    not null,
    package_name       varchar(30),
    package_size       numeric(10, 3),
    quantity_per_unit  numeric(12, 4) not null,
    waste_percent      numeric(5, 2)  not null,
    quantity           numeric(14, 4) not null,   -- consumo do item, ja com a perda
    unit_price         numeric(12, 2),
    cost               numeric(12, 2),
    sort_order         int            not null default 0
);

create index idx_estimate_item_material_item on estimate_item_material (item_id);
