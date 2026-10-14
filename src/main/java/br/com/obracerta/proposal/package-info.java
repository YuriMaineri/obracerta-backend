/** Geração da proposta em PDF; lê orçamento e empresa e nunca é chamado por eles. */
@org.springframework.modulith.ApplicationModule(
        displayName = "Proposal",
        allowedDependencies = {"shared", "estimate", "company"})
package br.com.obracerta.proposal;
