alter table company
    add column trade_name         varchar(200),
    add column signatory_name     varchar(150),
    add column district           varchar(100),
    add column postal_code        varchar(10),
    add column logo_content_type  varchar(50);

alter table clause
    add column active boolean not null default true;

create index idx_bank_account_company on bank_account (company_id);
create index idx_clause_company on clause (company_id, type, sort_order);
