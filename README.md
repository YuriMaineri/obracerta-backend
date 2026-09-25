# ObraCerta

Orçamentação para pequenas empresas de obras e reformas.
Monólito modular em Spring Boot 4.1 + Spring Modulith 2.1, Java 21, PostgreSQL 17 e Angular 22.

## Pré-requisitos (macOS)

| Ferramenta | Versão | Como conferir |
|---|---|---|
| JDK | 21 | `java -version` |
| Docker Desktop | qualquer recente | `docker compose version` |
| Node.js | 22.22.3+ ou 24.15+ | `node -v` |

Maven não precisa estar instalado: o `./mvnw` baixa a versão certa na primeira execução.

## Subir tudo (três terminais)

```bash
# 1. Banco (porta 5432)
docker compose up -d

# 2. API (porta 8080). O Flyway cria as tabelas sozinho na primeira subida.
cd backend && ./mvnw spring-boot:run

# 3. Tela (porta 4200). O /api é repassado para a 8080 pelo proxy.conf.json.
cd frontend && npm install && npm start
```

Abra http://localhost:4200.

Para cadastrar de uma vez os clientes dos orçamentos reais (com a API no ar):

```bash
sh scripts/seed-customers.sh
```

Conferir no banco:

```bash
docker exec -it obracerta-db psql -U obracerta -c "select id, name, tax_id from customer;"
```

## Testes

```bash
cd backend && ./mvnw test      # modularidade + regras de cliente + controller
cd frontend && npm test -- --watch=false
```

O `ModularidadeTest` falha se algum módulo acessar outro fora do declarado em
`package-info.java` e gera os diagramas em `backend/target/spring-modulith-docs`.

## API de clientes (`/api/customers`)

| Método | Caminho | Observação |
|---|---|---|
| GET | `/api/customers?query=&page=0&size=20&sort=name` | só ativos |
| GET | `/api/customers/{id}` | 404 se não existir |
| POST | `/api/customers` | 201 + `Location`; 400 se nome vazio ou CPF/CNPJ com tamanho errado |
| PUT | `/api/customers/{id}` | |
| DELETE | `/api/customers/{id}` | inativa, não apaga |

Erros saem no formato ProblemDetail; a mensagem legível, em português, está em `detail`.
CPF/CNPJ é gravado só com dígitos e formatado na tela.

## Estrutura dos módulos

```
br.com.obracerta
├── shared     (aberto)  enums, tipos transversais e tratamento de erros HTTP
├── company              empresa: dados, contas bancárias, cláusulas
├── customers            clientes e obras
├── catalog              catálogo: insumos, serviços e composições
├── estimate             orçamento: blocos, ambientes, itens, exclusões, notas internas
└── proposal             proposta: geração dos dois documentos
```

Regra: cada módulo expõe só o que está no pacote raiz. O que estiver em
`<modulo>/internal` é invisível para os demais — é o caso de `CustomerRepository`.
O módulo `pricing` (motor de cálculo) entra na Fatia 2.

## Convenção de nomes

Código, banco e API em inglês; comentários, README e textos da tela em português.

| Negócio (PT) | Código (EN) | Tabela |
|---|---|---|
| Empresa | `Company` | `company` |
| Conta bancária | `BankAccount` | `bank_account` |
| Cláusula | `Clause` | `clause` |
| Cliente | `Customer` | `customer` |
| Obra | `JobSite` | `job_site` |
| Insumo | `Material` | `material` |
| Histórico de preço | `MaterialPriceHistory` | `material_price_history` |
| Serviço | `Service` | `service` |
| Composição | `ServiceMaterial` | `service_material` |
| Orçamento | `Estimate` | `estimate` |
| Bloco do orçamento | `EstimateSection` | `estimate_section` |
| Ambiente | `Area` | `area` |
| Item do orçamento | `EstimateItem` | `estimate_item` |
| Material do orçamento | `EstimateMaterial` | `estimate_material` |
| Exclusão | `Exclusion` | `exclusion` |
| Nota interna | `InternalNote` | `internal_note` |
| Proposta | `Proposal` | — |
| Modo de cálculo (medida / peça / verba) | `PricingMode` (`BY_MEASUREMENT` / `PER_PIECE` / `LUMP_SUM`) | — |
| Regime de material | `MaterialSupply` (`CUSTOMER_SUPPLIED` / `ITEMIZED` / `ESTIMATED` / `BY_RECEIPT`) | — |
| PF / PJ | `PersonType` (`INDIVIDUAL` / `COMPANY`) | — |
| CPF / CNPJ | `taxId` | `tax_id` |
| A/C | `contactPerson` | `contact_person` |

## Notas de Boot 4 que já custaram tempo

- O Flyway precisa de `spring-boot-starter-flyway`. Só com `flyway-core`, as migrações não rodam
  e o `ddl-auto: validate` quebra dizendo que as tabelas não existem.
- `spring-boot-starter-web` foi substituído por `spring-boot-starter-webmvc`.
- `spring-modulith-starter-jpa` exige a tabela `event_publication`. Fica fora até o primeiro evento
  entre módulos; nesse dia, criar a tabela numa migração nova.
