/**
 * Tipos compartilhados por todos os modulos: enums e objetos de valor do dominio.
 *
 * <p>E um modulo aberto: seus subpacotes podem ser acessados livremente. Por isso,
 * so entra aqui o que for realmente transversal e estavel. Regra pratica: se o tipo
 * pertence a um unico modulo, ele nao mora aqui.
 */
@org.springframework.modulith.ApplicationModule(
        type = org.springframework.modulith.ApplicationModule.Type.OPEN,
        displayName = "Shared")
package br.com.obracerta.shared;
