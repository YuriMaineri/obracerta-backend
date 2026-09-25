package br.com.obracerta;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Dois testes que sustentam a arquitetura do projeto.
 *
 * <p>O primeiro falha se um modulo acessar outro fora do que foi declarado em
 * package-info.java, ou se surgir ciclo entre modulos. Isso e o que impede o
 * monolito modular de virar um monolito comum ao longo do semestre.
 *
 * <p>O segundo gera a documentacao da arquitetura a partir do proprio codigo,
 * em target/spring-modulith-docs: diagramas PlantUML dos modulos e um canvas por
 * modulo. Serve direto na secao de arquitetura do artigo, e nunca fica
 * desatualizada, porque e gerada do codigo real.
 */
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
