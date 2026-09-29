package br.com.obracerta.proposal;

import br.com.obracerta.TestcontainersConfiguration;
import br.com.obracerta.customers.Customer;
import br.com.obracerta.customers.CustomerRequest;
import br.com.obracerta.customers.CustomerService;
import br.com.obracerta.estimate.EstimateDetails;
import br.com.obracerta.estimate.EstimateRequest;
import br.com.obracerta.estimate.EstimateService;
import br.com.obracerta.shared.MaterialSupply;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class ProposalIntegrationTest {

    @Autowired
    ProposalService proposals;

    @Autowired
    EstimateService estimates;

    @Autowired
    CustomerService customers;

    EstimateDetails cartorio;

    @BeforeEach
    void createCartorioEstimate() {
        Long customerId = customers.create(new CustomerRequest(Customer.PersonType.COMPANY,
                "Cartório de Registro Civil 2ª Zona", null, null, null, null,
                "Rua Venâncio Aires 243", "Cidade Baixa", "Porto Alegre", null)).getId();
        cartorio = estimates.create(new EstimateRequest(customerId, null, LocalDate.of(2026, 8, 20), null, 4, 5,
                MaterialSupply.ESTIMATED, null, null, null, null,
                List.of(new EstimateRequest.ServiceLine("Pintura do teto da sala arquivo no 1º andar", "Ver com o gesseiro"),
                        new EstimateRequest.ServiceLine("Instalação de uma luminária.", null)),
                List.of(new EstimateRequest.MaterialLine("Tinta acrílica fosca e esmalte.", null, null)),
                List.of(new EstimateRequest.PriceLine("Valor da mão de obra de elétrica e pintura", new BigDecimal("1800.00")),
                        new EstimateRequest.PriceLine("Valor de material para pintura e elétrica", new BigDecimal("900.00"))),
                List.of("Problemas extras que aparecer será comunicado."),
                List.of("Levar escada grande")));
    }

    @Test
    void pdfFollowsTheCurrentDocumentAndHidesInternalNotes() throws IOException {
        GeneratedProposal proposal = proposals.generate(cartorio.id());

        assertThat(proposal.fileName()).startsWith("Orcamento-" + cartorio.number()).endsWith(".pdf");
        String text = extractText(proposal.content());
        assertThat(text)
                .contains("Cartório de Registro Civil 2ª Zona")
                .contains("Pintura do teto da sala arquivo no 1º andar")
                .contains("Valor da mão de obra de elétrica e pintura")
                .contains("R$ 1.800,00")
                .contains("R$ 2.700,00")
                .contains("Página 1 de")
                .contains("4 a 5 dias úteis")
                .contains("Depósito bancário, Banrisul agência 0047")
                .contains("Garantia sobre o serviço executado.")
                .contains("Problemas extras que aparecer será comunicado.")
                .contains("Porto Alegre, 20/08/2026.")
                .contains("NR-10")
                .doesNotContain("Ver com o gesseiro")
                .doesNotContain("Levar escada grande");
        assertThat(text.replaceAll("\\s", "")).contains("SERVIÇOASEREXECUTADO");
    }

    @Test
    void cardClauseComesAfterBankDetails() throws IOException {
        String text = extractText(proposals.generate(cartorio.id()).content());

        assertThat(text.indexOf("Depósito bancário")).isLessThan(text.indexOf("cartão de crédito"));
    }

    @Test
    void leadTimeAndMoneyFormatting() {
        assertThat(ProposalService.leadTime(4, 5)).isEqualTo("4 a 5 dias úteis");
        assertThat(ProposalService.leadTime(null, 1)).isEqualTo("1 dia útil");
        assertThat(ProposalService.leadTime(null, null)).isNull();
        assertThat(ProposalService.money(new BigDecimal("13157.57"))).isEqualTo("R$ 13.157,57");
        assertThat(ProposalService.slug("Cartório de Registro Civil 2ª Zona")).isEqualTo("Cartorio-de-Registro-Civil-2a-Zona");
        assertThat(ProposalService.districtAndCity("Cidade Baixa", "Porto Alegre")).isEqualTo("Bairro: Cidade Baixa. Porto Alegre.");
        assertThat(ProposalService.districtAndCity(null, "Porto Alegre")).isEqualTo("Porto Alegre.");
    }

    private static String extractText(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document).replace(' ', ' ').replaceAll("[ \\t]+", " ");
        }
    }
}
