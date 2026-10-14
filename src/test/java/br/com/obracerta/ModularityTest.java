package br.com.obracerta;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/** Garante as fronteiras entre os módulos e gera a documentação da arquitetura em target/spring-modulith-docs. */
class ModularityTest {

    ApplicationModules modules = ApplicationModules.of(ObraCertaApplication.class);

    @Test
    void verifiesModuleStructure() {
        modules.verify();
    }

    @Test
    void writesArchitectureDocumentation() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases();
    }

    @Test
    void printsModules() {
        modules.forEach(System.out::println);
    }
}
