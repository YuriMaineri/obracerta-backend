/**
 * Geracao dos documentos do orcamento.
 *
 * <p>Dois documentos, uma unica fonte de dados: a memoria de calculo interna, com
 * custo e margem, e a proposta comercial do cliente, agrupada e sem custo exposto.
 *
 * <p>Este modulo le o orcamento e os dados da empresa, e nunca e chamado por eles.
 * Manter essa direcao evita ciclo entre orcamento e proposta.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Proposal",
        allowedDependencies = {"shared", "estimate", "company"})
package br.com.obracerta.proposal;
