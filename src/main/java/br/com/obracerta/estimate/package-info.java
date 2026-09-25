/**
 * Montagem do orcamento: blocos, ambientes, itens, exclusoes e notas internas.
 *
 * <p>Um orcamento nao e um total unico. Ele se organiza em blocos que podem ser
 * obrigatorios, opcionais ou alternativos entre si, porque a empresa apresenta ao
 * cliente valores como "geral R$ 33.500" e "sem pintura das grades R$ 28.500".
 *
 * <p>As notas internas ficam aqui e nunca sao expostas na proposta do cliente.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Estimate",
        allowedDependencies = {"shared", "customers", "catalog"})
package br.com.obracerta.estimate;
