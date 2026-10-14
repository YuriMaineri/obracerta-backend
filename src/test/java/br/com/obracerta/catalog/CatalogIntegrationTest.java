package br.com.obracerta.catalog;

import br.com.obracerta.TestcontainersConfiguration;
import br.com.obracerta.shared.PricingMode;
import br.com.obracerta.shared.Unit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CatalogIntegrationTest {

    @Autowired
    CatalogService catalog;

    @Test
    void seedLoadsReferenceCatalog() {
        assertThat(catalog.listMaterials(null)).hasSize(46);
        assertThat(catalog.listServices(null, null)).hasSize(30);

        ServiceDetails wall = catalog.listServices("Pintura de parede interna", null).getFirst();
        assertThat(wall.pricingMode()).isEqualTo(PricingMode.BY_MEASUREMENT);
        assertThat(wall.daysPerUnit()).isEqualByComparingTo("0.0357");
        // 0,16 L × 1,05 × R$ 24,00 + 0,1 lixa × R$ 2,50 + 0,02 fita × R$ 9,00
        assertThat(wall.materialCostPerUnit()).isEqualByComparingTo("4.46");
        assertThat(catalog.listServices(null, ServiceCategory.ELECTRICAL)).hasSize(9);
    }

    @Test
    void serviceCostComesFromCompositionAndUnitDefinesPricingMode() {
        Material cable = catalog.createMaterial(new MaterialRequest("Cabo PP 3 x 2,5 mm²", Unit.METER, new BigDecimal("8.00"), null));
        Material box = catalog.createMaterial(new MaterialRequest("Caixa de passagem", Unit.PIECE, null, null));

        ServiceDetails created = catalog.createService(new ServiceRequest("Extensão de tomada externa",
                ServiceCategory.ELECTRICAL, Unit.POINT, null, new BigDecimal("0.33333"), null, List.of(
                        new ServiceRequest.CompositionLine(cable.getId(), new BigDecimal("5"), new BigDecimal("10")),
                        new ServiceRequest.CompositionLine(box.getId(), BigDecimal.ONE, null))));

        assertThat(created.pricingMode()).isEqualTo(PricingMode.PER_PIECE);
        assertThat(created.daysPerUnit()).isEqualByComparingTo("0.3333");
        assertThat(created.composition()).extracting(ServiceDetails.Line::materialName)
                .containsExactly("Cabo PP 3 x 2,5 mm²", "Caixa de passagem");
        assertThat(created.materialCostPerUnit()).isEqualByComparingTo("44.00");
        assertThat(created.missingPrices()).isTrue();
    }

    @Test
    void compositionCanBeReorderedAndChangedInPlace() {
        Material a = catalog.createMaterial(new MaterialRequest("Insumo A", Unit.KILOGRAM, BigDecimal.TEN, null));
        Material b = catalog.createMaterial(new MaterialRequest("Insumo B", Unit.LITER, BigDecimal.ONE, null));
        ServiceDetails s = catalog.createService(new ServiceRequest("Serviço teste", ServiceCategory.OTHER,
                Unit.SQUARE_METER, null, null, null, List.of(
                        new ServiceRequest.CompositionLine(a.getId(), BigDecimal.ONE, null),
                        new ServiceRequest.CompositionLine(b.getId(), BigDecimal.ONE, null))));

        ServiceDetails updated = catalog.updateService(s.id(), new ServiceRequest("Serviço teste", ServiceCategory.OTHER,
                Unit.SQUARE_METER, null, null, null, List.of(
                        new ServiceRequest.CompositionLine(b.getId(), new BigDecimal("2"), null))));

        assertThat(updated.composition()).extracting(ServiceDetails.Line::materialName).containsExactly("Insumo B");
        assertThat(updated.materialCostPerUnit()).isEqualByComparingTo("2.00");
    }

    @Test
    void repeatedMaterialAndLaborUnitsAreRejected() {
        Material a = catalog.createMaterial(new MaterialRequest("Insumo repetido", Unit.PIECE, BigDecimal.ONE, null));
        var line = new ServiceRequest.CompositionLine(a.getId(), BigDecimal.ONE, null);

        assertThatThrownBy(() -> catalog.createService(new ServiceRequest("Duplicado", ServiceCategory.OTHER,
                Unit.PIECE, null, null, null, List.of(line, line))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("mais de uma vez");
        assertThatThrownBy(() -> catalog.createService(new ServiceRequest("Por quilo", ServiceCategory.OTHER,
                Unit.KILOGRAM, null, null, null, List.of())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void priceChangesAreKeptInHistory() {
        Material paint = catalog.createMaterial(new MaterialRequest("Tinta teste", Unit.LITER, new BigDecimal("20.00"), null));

        catalog.updateMaterial(paint.getId(), new MaterialRequest("Tinta teste", Unit.LITER, new BigDecimal("20.00"), "Loja X"));
        catalog.updateMaterial(paint.getId(), new MaterialRequest("Tinta teste", Unit.LITER, new BigDecimal("23.50"), "Loja X"));

        assertThat(catalog.priceHistory(paint.getId())).extracting(MaterialPrice::getPrice)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("23.50"), new BigDecimal("20.00"));
    }

    @Test
    void materialInUseCannotBeRemoved() {
        Material paint = catalog.listMaterials("Tinta acrílica fosca").getFirst();

        assertThatThrownBy(() -> catalog.deactivateMaterial(paint.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pintura de parede interna");
    }
}
