/** Montagem do orçamento; as notas internas nunca vão para a proposta. */
@org.springframework.modulith.ApplicationModule(
        displayName = "Estimate",
        allowedDependencies = {"shared", "customers", "catalog", "company"})
package br.com.obracerta.estimate;
