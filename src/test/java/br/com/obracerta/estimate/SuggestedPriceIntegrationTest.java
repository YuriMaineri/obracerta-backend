package br.com.obracerta.estimate;

import br.com.obracerta.TestcontainersConfiguration;
import br.com.obracerta.catalog.CatalogService;
import br.com.obracerta.customers.Customer;
import br.com.obracerta.customers.CustomerRequest;
import br.com.obracerta.customers.CustomerService;
import br.com.obracerta.shared.Difficulty;
import br.com.obracerta.shared.MaterialSupply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class SuggestedPriceIntegrationTest {

    @Autowired
    EstimateService estimates;

    @Autowired
    CatalogService catalog;

    @Autowired
    CustomerService customers;

    Long customerId;
    Long wallPainting;
    Long dumpster;

    @BeforeEach
    void setUp() {
        customerId = customers.create(new CustomerRequest(Customer.PersonType.INDIVIDUAL, "Sr. Bernardo Santos",
                null, null, null, null, null, null, "Porto Alegre", null)).getId();
        wallPainting = catalog.listServices("Pintura de parede interna", null).getFirst().id();
        dumpster = catalog.listServices("Remoção de entulho com caçamba", null).getFirst().id();
    }

    @Test
    void catalogLinesGetSuggestedPriceAndFreeTextDoesNot() {
        EstimateDetails saved = estimates.create(request(MaterialSupply.ITEMIZED, List.of(
                new EstimateRequest.ServiceLine("Pintura do corredor social", null, wallPainting,
                        new BigDecimal("30"), Difficulty.HARD),
                new EstimateRequest.ServiceLine("Remoção do entulho", null, dumpster, null, null),
                new EstimateRequest.ServiceLine("Reboco das trincas", null))));

        assertThat(saved.services()).extracting(EstimateDetails.Service::suggestedPrice)
                .usingElementComparator((a, b) -> a == null || b == null ? (a == b ? 0 : 1) : a.compareTo(b))
                .containsExactly(new BigDecimal("761.99"), new BigDecimal("679.94"), null);
        assertThat(saved.services().get(1).quantity()).isEqualByComparingTo("1");
        assertThat(saved.services().get(1).difficulty()).isEqualTo(Difficulty.NORMAL);
        assertThat(saved.suggestedTotal()).isEqualByComparingTo("1441.93");
        assertThat(saved.suggestedDays()).isEqualByComparingTo("1.589");
    }

    @Test
    void savesTheCalculationMemoryAndTheShoppingList() {
        EstimateDetails saved = estimates.create(request(MaterialSupply.ITEMIZED, List.of(
                new EstimateRequest.ServiceLine("Pintura do corredor", null, wallPainting, new BigDecimal("30"), Difficulty.HARD),
                new EstimateRequest.ServiceLine("Pintura do lavabo", null, wallPainting, new BigDecimal("12"), null),
                new EstimateRequest.ServiceLine("Reboco das trincas", null))));

        EstimateDetails.Service corridor = saved.services().getFirst();
        assertThat(corridor.laborDays()).isEqualByComparingTo("1.339");
        assertThat(corridor.laborCost()).isEqualByComparingTo("468.56");
        assertThat(corridor.materialCost()).isEqualByComparingTo("133.80");
        assertThat(corridor.materials()).extracting(EstimateDetails.MaterialUse::material)
                .containsExactly("Tinta acrílica fosca", "Lixa (folha)", "Fita crepe");
        EstimateDetails.MaterialUse paint = corridor.materials().getFirst();
        assertThat(paint.quantity()).isEqualByComparingTo("5.04");
        assertThat(paint.wastePercent()).isEqualByComparingTo("5");
        assertThat(paint.unitPrice()).isEqualByComparingTo("24.00");
        assertThat(paint.cost()).isEqualByComparingTo("120.90");
        assertThat(corridor.materials().stream().map(EstimateDetails.MaterialUse::cost).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo(corridor.materialCost());
        assertThat(saved.services().get(2).materials()).isEmpty();

        // 5,04 L + 2,016 L = 7,056 L de tinta → 2 galões de 3,6 L; 3 + 1,2 folhas de lixa → 5
        assertThat(saved.shoppingList()).extracting(EstimateDetails.ShoppingItem::material)
                .containsExactly("Tinta acrílica fosca", "Lixa (folha)", "Fita crepe");
        EstimateDetails.ShoppingItem paintToBuy = saved.shoppingList().getFirst();
        assertThat(paintToBuy.needed()).isEqualByComparingTo("7.056");
        assertThat(paintToBuy.packageName()).isEqualTo("galão");
        assertThat(paintToBuy.packages()).isEqualByComparingTo("2");
        assertThat(paintToBuy.leftover()).isEqualByComparingTo("0.144");
        assertThat(paintToBuy.cost()).isEqualByComparingTo("169.26");
        assertThat(paintToBuy.purchaseCost()).isEqualByComparingTo("172.80");
        assertThat(saved.shoppingList().get(1).packages()).isEqualByComparingTo("5");

        EstimateDetails reloaded = estimates.findById(saved.id());
        assertThat(reloaded.services().getFirst().materials()).hasSize(3);
        assertThat(reloaded.shoppingList()).hasSize(3);
    }

    @Test
    void duplicateKeepsTheCatalogLink() {
        EstimateDetails saved = estimates.create(request(MaterialSupply.CUSTOMER_SUPPLIED, List.of(
                new EstimateRequest.ServiceLine("Pintura do corredor", null, wallPainting, new BigDecimal("30"), null))));

        EstimateDetails copy = estimates.duplicate(saved.id());

        assertThat(copy.services().getFirst().serviceId()).isEqualTo(wallPainting);
        assertThat(copy.suggestedTotal()).isEqualByComparingTo("474.19");
    }

    @Test
    void measuredServiceNeedsQuantity() {
        assertThatThrownBy(() -> estimates.create(request(MaterialSupply.ITEMIZED, List.of(
                new EstimateRequest.ServiceLine("Pintura", null, wallPainting, null, null)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantidade");
    }

    private EstimateRequest request(MaterialSupply supply, List<EstimateRequest.ServiceLine> services) {
        return new EstimateRequest(customerId, null, LocalDate.of(2026, 10, 14), null, null, null, supply, null, null,
                null, null, services, List.of(), List.of(), List.of(), List.of());
    }
}
