-- Dados reais extraidos dos orcamentos emitidos entre mar e ago/2026. Editaveis pela tela.
insert into company (legal_name, trade_name, tax_id, phone, city, signatory_name)
values ('Carlos Alberto Maineri da Silva', 'Maineri Elétrica, Construções e Reformas',
        '24839705000165', '51984232827', 'Porto Alegre', 'Carlos Maineri');

insert into bank_account (company_id, bank, branch, account_number, account_type, pix_key, holder, is_default)
select id, 'Banrisul', '0047', '060740190-5', 'Conta corrente jurídica', '24839705000165',
       'Carlos Alberto Maineri da Silva', true
from company;

insert into bank_account (company_id, bank, branch, account_number, account_type, pix_key, holder, is_default)
select id, 'SICREDI', '0116', '56780-7', 'Conta corrente jurídica', null,
       'Carlos Alberto Maineri da Silva', false
from company;

insert into clause (company_id, type, title, content, is_default, sort_order)
select c.id, v.type, v.title, v.content, v.is_default, v.sort_order
from company c
cross join (values
    ('WARRANTY', 'Garantia', 'Garantia sobre o serviço executado.', true, 1),

    ('PAYMENT', 'Na entrega', 'Na entrega do serviço executado.', true, 1),
    ('PAYMENT', 'Na entrega com nota fiscal', 'Na entrega do serviço, com emissão de nota fiscal.', false, 2),
    ('PAYMENT', '50% no início e 50% na entrega', 'Mão de obra 50% no início do serviço e 50% na entrega.', false, 3),
    ('PAYMENT', '50% no início e 50% em 30 dias', '50% no início do serviço e 50% 30 dias após.', false, 4),
    ('PAYMENT', 'Faturado 30 dias', 'Faturado 30 dias.', false, 5),
    ('PAYMENT', 'Material com nota semanal', 'Material apresento as notas semanalmente.', false, 6),
    ('PAYMENT', 'Cartão de crédito', 'Com cartão de crédito encargos por conta do cliente.', true, 7),

    ('OBSERVATION', 'Serviços extras', 'Serviços extras e problemas que vierem ocorrer durante a execução do trabalho, que não estejam relacionados no orçamento, será cobrado a parte, mas antes será comunicado ao cliente.', true, 1),
    ('OBSERVATION', 'Local para os colaboradores', 'Preciso um lugar, para que os colaboradores possam trocar de roupa e ter onde almoçar.', false, 2),
    ('OBSERVATION', 'Descarte de caliça', 'Descarte de caliça em local apropriado.', false, 3),
    ('OBSERVATION', 'Sem projeto elétrico', 'Não foi apresentado projeto elétrico de execução.', false, 4),
    ('OBSERVATION', 'Infraestrutura para fiação', 'Para execução das passagens da fiação, deverá estar toda infra interna de tubulação pronta e de fácil acesso, livre de obstruções.', false, 5),

    ('REGULATORY', 'NR-10', 'NR-10 – Segurança em Instalações e Serviços em Eletricidade.', true, 1),
    ('REGULATORY', 'NR-18', 'NR 18 estabelece diretrizes de administração, de planejamento e de organização, relacionadas ao setor da construção civil.', true, 2),
    ('REGULATORY', 'NR-33', 'NR-33 é uma norma para trabalhos confinados.', true, 3),
    ('REGULATORY', 'NR-35', 'NR 35 é uma norma regulamentadora, que versa sobre padrões de segurança para o trabalho em altura.', true, 4)
) as v(type, title, content, is_default, sort_order);
