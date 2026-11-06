package br.com.obracerta.estimate;

import br.com.obracerta.catalog.CatalogService;
import br.com.obracerta.catalog.ServiceDetails;
import br.com.obracerta.company.BankAccount;
import br.com.obracerta.company.Clause;
import br.com.obracerta.company.CompanyService;
import br.com.obracerta.customers.Customer;
import br.com.obracerta.customers.CustomerService;
import br.com.obracerta.estimate.internal.EstimateRepository;
import br.com.obracerta.pricing.PriceCalculator;
import br.com.obracerta.pricing.PricingService;
import br.com.obracerta.pricing.Purchase;
import br.com.obracerta.pricing.Rates;
import br.com.obracerta.shared.Difficulty;
import br.com.obracerta.shared.MaterialSupply;
import br.com.obracerta.shared.PricingMode;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** API do módulo de orçamentos. Toda leitura sai já montada, porque as coleções são lazy. */
@Service
@Transactional(readOnly = true)
public class EstimateService {

    private final EstimateRepository repository;
    private final CustomerService customers;
    private final CompanyService company;
    private final CatalogService catalog;
    private final PricingService pricing;

    EstimateService(EstimateRepository repository, CustomerService customers, CompanyService company,
                    CatalogService catalog, PricingService pricing) {
        this.repository = repository;
        this.customers = customers;
        this.company = company;
        this.catalog = catalog;
        this.pricing = pricing;
    }

    public Page<EstimateSummary> search(String query, EstimateStatus status, Long customerId, Pageable pageable) {
        String term = query == null ? "" : query.trim().toLowerCase();
        Specification<Estimate> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }
            if (!term.isEmpty()) {
                String like = "%" + term + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("number")), like),
                        cb.like(cb.lower(cb.coalesce(root.<String>get("title"), "")), like)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<Estimate> page = repository.findAll(spec, pageable);
        Set<Long> customerIds = page.stream().map(Estimate::getCustomerId).collect(Collectors.toSet());
        Map<Long, Customer> names = customers.findAllByIds(customerIds);
        return page.map(e -> toSummary(e, names.get(e.getCustomerId())));
    }

    public EstimateDetails findById(Long id) {
        return toDetails(load(id));
    }

    @Transactional
    public EstimateDetails create(EstimateRequest request) {
        Estimate estimate = new Estimate(nextNumber(LocalDate.now()), request.customerId());
        Set<Long> clauseIds = request.clauseIds() == null ? defaultClauseIds() : request.clauseIds();
        Long bankAccountId = request.bankAccountId() == null
                ? company.findDefaultBankAccount().map(BankAccount::getId).orElse(null)
                : request.bankAccountId();
        apply(estimate, request, bankAccountId, clauseIds);
        return toDetails(repository.save(estimate));
    }

    @Transactional
    public EstimateDetails update(Long id, EstimateRequest request) {
        Estimate estimate = load(id);
        apply(estimate, request, request.bankAccountId(),
                request.clauseIds() == null ? Set.of() : request.clauseIds());
        return toDetails(estimate);
    }

    /** Substitui o hábito atual de copiar o arquivo do orçamento anterior. */
    @Transactional
    public EstimateDetails duplicate(Long id) {
        Estimate source = load(id);
        EstimateDetails d = toDetails(source);
        EstimateRequest copy = new EstimateRequest(d.customer().id(), d.title(), LocalDate.now(), d.validityDays(),
                d.minLeadDays(), d.maxLeadDays(), d.materialSupply(), d.estimatedMaterialCost(), d.paymentTerms(),
                d.bankAccountId(), d.clauseIds(),
                d.services().stream().map(s -> new EstimateRequest.ServiceLine(s.description(), s.internalNote(),
                        s.serviceId(), s.quantity(), s.difficulty())).toList(),
                d.materials().stream().map(m -> new EstimateRequest.MaterialLine(m.description(), m.quantity(), m.totalPrice(), m.materialId())).toList(),
                d.priceLines().stream().map(p -> new EstimateRequest.PriceLine(p.description(), p.amount(), p.alternative())).toList(),
                d.exclusions(), d.internalNotes());
        Set<Long> activeClauses = company.listClauses().stream().map(Clause::getId).collect(Collectors.toSet());
        Set<Long> clauseIds = copy.clauseIds().stream().filter(activeClauses::contains).collect(Collectors.toSet());
        Long bankAccountId = company.listBankAccounts().stream().anyMatch(a -> a.getId().equals(copy.bankAccountId()))
                ? copy.bankAccountId()
                : company.findDefaultBankAccount().map(BankAccount::getId).orElse(null);
        Estimate estimate = new Estimate(nextNumber(LocalDate.now()), copy.customerId());
        apply(estimate, copy, bankAccountId, clauseIds);
        return toDetails(repository.save(estimate));
    }

    @Transactional
    public EstimateDetails changeStatus(Long id, EstimateStatus status) {
        Estimate estimate = load(id);
        estimate.changeStatus(status);
        return toDetails(estimate);
    }

    @Transactional
    public void delete(Long id) {
        Estimate estimate = load(id);
        if (estimate.getStatus() != EstimateStatus.DRAFT) {
            throw new IllegalArgumentException("So orcamentos em rascunho podem ser excluidos; marque como recusado");
        }
        repository.delete(estimate);
    }

    private void apply(Estimate estimate, EstimateRequest request, Long bankAccountId, Set<Long> clauseIds) {
        customers.findById(request.customerId());
        if (bankAccountId != null) {
            company.findBankAccount(bankAccountId);
        }
        validateClauses(clauseIds);
        if (request.minLeadDays() != null && request.maxLeadDays() != null
                && request.minLeadDays() > request.maxLeadDays()) {
            throw new IllegalArgumentException("Prazo minimo maior que o prazo maximo");
        }
        estimate.updateHeader(request, bankAccountId);
        priceServices(estimate, orEmpty(request.services()), request.materialSupply());
        estimate.replaceMaterials(orEmpty(request.materials()));
        estimate.replacePriceLines(orEmpty(request.priceLines()));
        estimate.replaceExclusions(orEmpty(request.exclusions()));
        estimate.replaceInternalNotes(orEmpty(request.internalNotes()));
        estimate.replaceClauses(clauseIds);
    }

    private void priceServices(Estimate estimate, List<EstimateRequest.ServiceLine> lines,
                               MaterialSupply supply) {
        Rates rates = pricing.rates();
        boolean chargeMaterial = PriceCalculator.chargesMaterial(supply);
        List<ServiceEntry> entries = new ArrayList<>();
        List<PriceCalculator.ItemQuote> quotes = new ArrayList<>();
        for (EstimateRequest.ServiceLine line : lines) {
            String description = line.description().trim();
            String note = Estimate.blankToNull(line.internalNote());
            if (line.serviceId() == null) {
                entries.add(ServiceEntry.text(description, note));
                continue;
            }
            ServiceDetails service = catalog.findService(line.serviceId());
            BigDecimal quantity = service.pricingMode() == PricingMode.LUMP_SUM ? BigDecimal.ONE : line.quantity();
            if (quantity == null || quantity.signum() == 0) {
                throw new IllegalArgumentException("Informe a quantidade de \"%s\" (em %s)."
                        .formatted(service.name(), service.unit().getSymbol()));
            }
            Difficulty difficulty = line.difficulty() == null ? Difficulty.NORMAL : line.difficulty();
            var quote = PriceCalculator.item(rates, quantity, service.daysPerUnit(), service.materialCostPerUnit(),
                    difficulty, chargeMaterial);
            quotes.add(quote);
            entries.add(new ServiceEntry(description, note, service.id(), service.pricingMode(), quantity, difficulty,
                    quote, materialUses(service, quantity)));
        }
        estimate.replaceServices(entries);
        if (quotes.isEmpty()) {
            estimate.updateSuggestion(null, null);
        } else {
            var total = PriceCalculator.total(rates, quotes, chargeMaterial);
            estimate.updateSuggestion(total.total(), total.laborDays());
        }
    }

    private static List<EstimateDetails.MaterialUse> materialUses(ServiceDetails service, BigDecimal quantity) {
        return service.composition().stream().map(line -> {
            var use = PriceCalculator.material(quantity, line.quantityPerUnit(), line.wastePercent(), line.costPerUnit());
            return new EstimateDetails.MaterialUse(line.materialId(), line.materialName(), line.materialUnit(),
                    line.packageName(), line.packageSize(), line.quantityPerUnit(), line.wastePercent(),
                    use.quantity(), line.materialPrice(), use.cost());
        }).toList();
    }

    /** Soma o mesmo insumo em todos os serviços e arredonda para embalagem fechada. */
    static List<EstimateDetails.ShoppingItem> shoppingList(List<EstimateDetails.Service> services) {
        Map<Long, List<EstimateDetails.MaterialUse>> byMaterial = services.stream()
                .flatMap(s -> s.materials().stream())
                .collect(Collectors.groupingBy(EstimateDetails.MaterialUse::materialId, LinkedHashMap::new, Collectors.toList()));
        return byMaterial.values().stream().map(uses -> {
            var first = uses.getFirst();
            BigDecimal needed = uses.stream().map(EstimateDetails.MaterialUse::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal cost = uses.stream().anyMatch(u -> u.cost() == null) ? null
                    : uses.stream().map(EstimateDetails.MaterialUse::cost).reduce(BigDecimal.ZERO, BigDecimal::add);
            Purchase purchase = Purchase.of(first.unit(), first.packageSize(), needed, first.unitPrice());
            return new EstimateDetails.ShoppingItem(first.materialId(), first.material(), first.unit(),
                    first.packageName(), first.packageSize(), needed.stripTrailingZeros(), purchase.packages(),
                    purchase.bought(), purchase.leftover(), first.unitPrice(), cost, purchase.cost());
        }).toList();
    }

    private void validateClauses(Set<Long> clauseIds) {
        if (clauseIds.isEmpty()) {
            return;
        }
        Set<Long> known = company.listClauses().stream().map(Clause::getId).collect(Collectors.toSet());
        if (!known.containsAll(clauseIds)) {
            throw new IllegalArgumentException("Clausula inexistente ou removida");
        }
    }

    private Set<Long> defaultClauseIds() {
        return company.listClauses().stream()
                .filter(Clause::isDefaultClause)
                .map(Clause::getId)
                .collect(Collectors.toSet());
    }

    String nextNumber(LocalDate date) {
        String prefix = date.getYear() + "-";
        String last = repository.findLastNumberWithPrefix(prefix);
        int next = last == null ? 1 : Integer.parseInt(last.substring(prefix.length())) + 1;
        return prefix + String.format("%04d", next);
    }

    private Estimate load(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Orcamento nao encontrado: " + id));
    }

    private EstimateDetails toDetails(Estimate e) {
        Customer c = customers.findById(e.getCustomerId());
        var customer = new EstimateDetails.CustomerSummary(c.getId(), c.getName(), c.getPersonType().name(),
                c.getTaxId(), c.getContactPerson(), c.getAddress(), c.getDistrict(), c.getCity(), c.getPostalCode());
        var services = e.services().stream().map(s -> new EstimateDetails.Service(s.getDescription(),
                s.getInternalNote(), s.getServiceId(), s.getQuantity(), s.getDifficulty(), s.getSuggestedPrice(),
                s.getLaborDays(), s.getLaborCost(), s.getMaterialCost(), s.materialUses())).toList();
        return new EstimateDetails(e.getId(), e.getNumber(), e.getStatus(), customer, e.getTitle(), e.getIssueDate(),
                e.getValidityDays(), e.getMinLeadDays(), e.getMaxLeadDays(), e.getMaterialSupply(),
                e.getEstimatedMaterialCost(), e.getPaymentTerms(), e.getBankAccountId(), Set.copyOf(e.getClauseIds()),
                services,
                e.getMaterials().stream().map(m -> new EstimateDetails.Material(m.getDescription(), m.getQuantity(), m.getTotalPrice(), m.getMaterialId())).toList(),
                e.getPriceLines().stream().map(p -> new EstimateDetails.Price(p.getDescription(), p.getAmount(), p.isAlternative())).toList(),
                e.getExclusions().stream().map(Exclusion::getContent).toList(),
                e.getInternalNotes().stream().map(InternalNote::getContent).toList(),
                e.getSuggestedTotal(), e.getSuggestedDays(), shoppingList(services),
                e.getUpdatedAt());
    }

    private static EstimateSummary toSummary(Estimate e, Customer customer) {
        var prices = e.getPriceLines();
        return new EstimateSummary(e.getId(), e.getNumber(), e.getStatus(), e.getCustomerId(),
                customer == null ? null : customer.getName(), e.getTitle(), e.getIssueDate(),
                prices.stream().filter(p -> !p.isAlternative()).findFirst()
                        .or(() -> prices.stream().findFirst())
                        .map(EstimatePriceLine::getAmount).orElse(null),
                prices.size(),
                e.getMinLeadDays(), e.getMaxLeadDays(), e.getUpdatedAt());
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}
