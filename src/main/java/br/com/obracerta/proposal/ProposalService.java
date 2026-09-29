package br.com.obracerta.proposal;

import br.com.obracerta.company.BankAccount;
import br.com.obracerta.company.Clause;
import br.com.obracerta.company.ClauseType;
import br.com.obracerta.company.Company;
import br.com.obracerta.company.CompanyService;
import br.com.obracerta.estimate.EstimateDetails;
import br.com.obracerta.estimate.EstimateService;
import br.com.obracerta.shared.BrazilianDocuments;
import br.com.obracerta.shared.MaterialSupply;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/** Gera a proposta comercial do cliente, no formato dos orçamentos atuais. Notas internas nunca entram. */
@Service
@Transactional(readOnly = true)
public class ProposalService {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String FONT_FAMILY = "Libre Franklin";

    private final EstimateService estimates;
    private final CompanyService company;
    private final ITemplateEngine templates;

    ProposalService(EstimateService estimates, CompanyService company, ITemplateEngine templates) {
        this.estimates = estimates;
        this.company = company;
        this.templates = templates;
    }

    public GeneratedProposal generate(Long estimateId) {
        EstimateDetails estimate = estimates.findById(estimateId);
        String html = renderHtml(estimate);
        return new GeneratedProposal(fileName(estimate), toPdf(html));
    }

    String renderHtml(EstimateDetails estimate) {
        Context context = new Context(PT_BR);
        context.setVariable("p", buildView(estimate));
        return templates.process("proposal/estimate", context);
    }

    ProposalView buildView(EstimateDetails e) {
        Company c = company.getCompany();
        List<Clause> clauses = company.findClausesForDocument(e.clauseIds());
        Optional<BankAccount> account = e.bankAccountId() == null
                ? Optional.empty()
                : company.findBankAccountForDocument(e.bankAccountId());
        String companyTaxId = BrazilianDocuments.formatTaxId(c.getTaxId());

        var customer = e.customer();
        String docLabel = "INDIVIDUAL".equals(customer.personType()) ? "CPF" : "CNPJ";
        List<String> address = new ArrayList<>();
        addIfPresent(address, customer.address());
        addIfPresent(address, districtAndCity(customer.district(), customer.city()));
        if (customer.postalCode() != null) {
            address.add("CEP: " + BrazilianDocuments.formatPostalCode(customer.postalCode()));
        }

        List<ProposalView.Item> materialItems = e.materials().stream()
                .map(m -> new ProposalView.Item(m.description(), m.totalPrice() == null ? null : money(m.totalPrice())))
                .toList();
        BigDecimal materialSum = sum(e.materials().stream().map(EstimateDetails.Material::totalPrice));
        List<ProposalView.Item> priceItems = e.priceLines().stream()
                .map(p -> new ProposalView.Item(p.description(), p.amount() == null ? null : money(p.amount())))
                .toList();
        BigDecimal total = sum(e.priceLines().stream().map(EstimateDetails.Price::amount))
                .add(e.materialSupply() == MaterialSupply.ITEMIZED ? materialSum : BigDecimal.ZERO);

        List<String> payment = new ArrayList<>();
        if (e.paymentTerms() != null) {
            e.paymentTerms().lines().map(String::trim).filter(l -> !l.isEmpty()).forEach(payment::add);
        }
        List<String> paymentClauses = contents(clauses, ClauseType.PAYMENT);
        // Nos documentos atuais a cláusula de cartão fecha a forma de pagamento, depois dos dados bancários.
        paymentClauses.stream().filter(t -> !mentionsCard(t)).forEach(payment::add);
        account.ifPresent(a -> payment.addAll(bankLines(a, companyTaxId)));
        paymentClauses.stream().filter(ProposalService::mentionsCard).forEach(payment::add);

        List<String> observations = new ArrayList<>(contents(clauses, ClauseType.OBSERVATION));
        observations.addAll(e.exclusions());

        return new ProposalView(
                logoDataUri(c),
                c.getTradeName() != null ? c.getTradeName() : c.getLegalName(),
                companyTaxId,
                c.getPhone() == null ? null : BrazilianDocuments.formatPhone(c.getPhone()),
                customer.name(),
                customer.taxId() == null ? null : docLabel + ": " + BrazilianDocuments.formatTaxId(customer.taxId()),
                address,
                customer.contactPerson() == null ? null : "A/C " + customer.contactPerson(),
                e.title(),
                e.number(),
                e.issueDate().format(DATE),
                e.services().stream().map(EstimateDetails.Service::description).toList(),
                materialItems,
                materialSum.signum() > 0 ? money(materialSum) : null,
                materialNote(e),
                priceItems,
                total.signum() > 0 ? money(total) : null,
                leadTime(e.minLeadDays(), e.maxLeadDays()),
                payment,
                contents(clauses, ClauseType.WARRANTY),
                observations,
                contents(clauses, ClauseType.REGULATORY),
                (c.getCity() == null ? "" : c.getCity() + ", ") + e.issueDate().format(DATE) + ".",
                c.getSignatoryName() != null ? c.getSignatoryName() : c.getLegalName());
    }

    private static String materialNote(EstimateDetails e) {
        return switch (e.materialSupply()) {
            case CUSTOMER_SUPPLIED -> "Material será fornecido pela contratante.";
            case BY_RECEIPT -> "Material comprado pela empresa, com apresentação das notas durante a obra.";
            case ESTIMATED -> e.estimatedMaterialCost() == null
                    ? "Valor e quantitativo de material informados após a aprovação do orçamento."
                    : "Valor e quantitativo de material informados após a aprovação do orçamento (valor aproximado "
                      + money(e.estimatedMaterialCost()) + ").";
            case ITEMIZED -> null;
        };
    }

    private static List<String> bankLines(BankAccount a, String companyTaxId) {
        List<String> lines = new ArrayList<>();
        if (a.getPixKey() != null) {
            String key = BrazilianDocuments.digitsOnly(a.getPixKey());
            lines.add(key.length() == 14 && key.equals(a.getPixKey().replaceAll("[\\s./-]", ""))
                    ? "PIX: CNPJ " + BrazilianDocuments.formatTaxId(key)
                    : "PIX: " + a.getPixKey());
        }
        StringBuilder deposit = new StringBuilder("Depósito bancário, ").append(a.getBank());
        if (a.getBranch() != null) deposit.append(" agência ").append(a.getBranch());
        if (a.getAccountType() != null) deposit.append(' ').append(a.getAccountType().toLowerCase(PT_BR));
        if (a.getAccountNumber() != null) deposit.append(' ').append(a.getAccountNumber());
        if (companyTaxId != null) deposit.append(" CNPJ ").append(companyTaxId);
        deposit.append('.');
        if (a.getHolder() != null) deposit.append(' ').append(a.getHolder()).append('.');
        lines.add(deposit.toString());
        return lines;
    }

    static String leadTime(Integer min, Integer max) {
        Integer value = max != null ? max : min;
        if (value == null) {
            return null;
        }
        String unit = value == 1 ? "dia útil" : "dias úteis";
        return min != null && max != null && !min.equals(max) ? min + " a " + max + " " + unit : value + " " + unit;
    }

    static String money(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value).replace(' ', ' ');
    }

    private static BigDecimal sum(Stream<BigDecimal> values) {
        return values.filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean mentionsCard(String text) {
        return text.toLowerCase(PT_BR).contains("cartão");
    }

    private static List<String> contents(List<Clause> clauses, ClauseType type) {
        return clauses.stream().filter(c -> c.getType() == type).map(Clause::getContent).toList();
    }

    private static String logoDataUri(Company c) {
        return c.hasLogo()
                ? "data:" + c.getLogoContentType() + ";base64," + Base64.getEncoder().encodeToString(c.getLogo())
                : null;
    }

    /** No formato dos orçamentos atuais: "Bairro: Cidade Baixa. Porto Alegre." */
    static String districtAndCity(String district, String city) {
        List<String> parts = new ArrayList<>();
        if (district != null && !district.isBlank()) parts.add("Bairro: " + district.trim() + ".");
        if (city != null && !city.isBlank()) parts.add(city.trim() + ".");
        return String.join(" ", parts);
    }

    private static void addIfPresent(List<String> list, String value) {
        if (value != null && !value.isBlank()) list.add(value);
    }

    private static String fileName(EstimateDetails e) {
        String customer = slug(e.customer().name());
        return "Orcamento-" + e.number() + (customer.isEmpty() ? "" : "-" + customer) + ".pdf";
    }

    private static byte[] toPdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(() -> font("LibreFranklin-Regular.ttf"), FONT_FAMILY, 400, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> font("LibreFranklin-Medium.ttf"), FONT_FAMILY, 500, BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao gerar o PDF da proposta", ex);
        }
    }

    private static InputStream font(String file) {
        return ProposalService.class.getResourceAsStream("/fonts/" + file);
    }

    /** Nome de arquivo sem acentos nem espaços. */
    static String slug(String text) {
        String ascii = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
        return ascii.replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
