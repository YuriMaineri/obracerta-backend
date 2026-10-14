-- Linha de valor alternativa: opcao com servicos a menos, mostrada no PDF mas fora do total.
alter table estimate_price_line
    add column alternative boolean not null default false;
