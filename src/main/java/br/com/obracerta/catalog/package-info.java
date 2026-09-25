/**
 * Catalogo de insumos, servicos e composicoes de custo do proprio prestador.
 *
 * <p>Comeca vazio e e preenchido aos poucos pela propria tela, servico a servico,
 * conforme a empresa for usando o sistema. Nenhuma base publica de precos e
 * embarcada: o diferencial do produto e a composicao propria.
 *
 * <p>Modulo folha: e consultado pelo orcamento, mas nao conhece o orcamento.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Catalog",
        allowedDependencies = {"shared"})
package br.com.obracerta.catalog;
